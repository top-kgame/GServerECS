#!/usr/bin/env python3
"""ECS 性能影响验证脚本。

- full：JUnit performance 套件，默认经 -Pperf；支持 --repeat 多 JVM 中位数
- core：JMH world.update 核心基准，经 -Pjmh（不进默认 mvn test）
- --label / --diff：打基线并对比改代码前后

默认 mvn test 已排除 ecstest.performance；本脚本才是压测入口。
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import statistics
import subprocess
import sys
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path

PERFORMANCE_TEST_DIR = Path("src/test/java/top/kgame/lib/ecstest/performance")
JMH_RESULT_JSON = Path("target/jmh-result.json")
LABELS_DIRNAME = "labels"

METRIC_RE = re.compile(
    r"基准|测量结果|性能测试|加速比|加速|throughput|ops/|实体/秒|迁移/秒|理论每秒"
)
LOG_MSG_RE = re.compile(r"\| (.+)$")
META_ROW_RE = re.compile(r"^\|\s*(.+?)\s*\|\s*(.+?)\s*\|$")

PRIMARY_METRIC_PATTERNS: list[tuple[str, re.Pattern[str], bool]] = [
    ("avg_ms", re.compile(r"平均每次\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ms"), True),
    ("avg_ms", re.compile(r"平均耗时\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ms"), True),
    ("avg_round_ms", re.compile(r"平均每轮\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ms"), True),
    ("avg_us", re.compile(r"平均每次\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*us"), True),
    ("avg_ns", re.compile(r"平均(?:每次扫)?\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ns"), True),
    ("total_ms", re.compile(r"总耗时\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ms"), True),
    ("jmh_us", re.compile(r"([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*us/op"), True),
    ("jmh_ns", re.compile(r"([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ns/op"), True),
    ("jmh_ms", re.compile(r"([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*ms/op"), True),
    ("throughput", re.compile(r"吞吐约\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)"), False),
    ("updates_per_sec", re.compile(r"理论每秒更新\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)"), False),
    ("speedup", re.compile(r"加速比(?:\(参考\))?[:\s]+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)"), False),
]

# 结果 Markdown 写入格式: "12.3 (jmh_us)" / "0.5 (avg_ms)"
STORED_METRIC_RE = re.compile(
    r"^([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)\s*\((\w+)\)"
)
STORED_FIELD_DIRECTION: dict[str, bool] = {
    "avg_ms": True,
    "avg_round_ms": True,
    "avg_us": True,
    "avg_ns": True,
    "total_ms": True,
    "jmh_us": True,
    "jmh_ns": True,
    "jmh_ms": True,
    "throughput": False,
    "updates_per_sec": False,
    "speedup": False,
}

# full 套件（多 JVM 中位数后）默认门槛
DEFAULT_NOISE_REL_PCT = 15.0
DEFAULT_SIG_REL_PCT = 30.0
# core / JMH 门槛
CORE_NOISE_REL_PCT = 5.0
CORE_SIG_REL_PCT = 10.0

DEFAULT_ABS_MS = 0.05
DEFAULT_ABS_US = 0.5
DEFAULT_ABS_NS = 50.0
DEFAULT_ABS_SPEEDUP = 0.1
DEFAULT_THROUGHPUT_ABS_REL_PCT = 5.0

CORE_KEY_RE = re.compile(
    r"SystemUpdate|ParallelUpdate 基准|TickRate 混合|WorldUpdate|ParallelWorldUpdate"
)

TREND_SORT_ORDER = {
    "显著变差": 0,
    "变差": 1,
    "显著变好": 2,
    "变好": 3,
    "持平": 4,
    "仅参考": 5,
    "字段不可比": 6,
    "新增": 7,
    "缺失": 8,
}


@dataclass(frozen=True)
class NoiseThresholds:
    noise_rel_pct: float = DEFAULT_NOISE_REL_PCT
    sig_rel_pct: float = DEFAULT_SIG_REL_PCT
    abs_ms: float = DEFAULT_ABS_MS
    abs_us: float = DEFAULT_ABS_US
    abs_ns: float = DEFAULT_ABS_NS
    abs_speedup: float = DEFAULT_ABS_SPEEDUP
    throughput_abs_rel_pct: float = DEFAULT_THROUGHPUT_ABS_REL_PCT
    core_noise_rel_pct: float = CORE_NOISE_REL_PCT
    core_sig_rel_pct: float = CORE_SIG_REL_PCT


@dataclass(frozen=True)
class ParsedMetric:
    key: str
    value: float
    metric_field: str
    lower_is_better: bool
    raw: str
    sample_count: int = 1
    min_value: float | None = None
    max_value: float | None = None


@dataclass
class MetricAccumulator:
    values: list[float] = field(default_factory=list)
    metric_field: str = ""
    lower_is_better: bool = True
    raw_samples: list[str] = field(default_factory=list)


def discover_performance_tests(repo_root: Path) -> list[str]:
    root = repo_root / PERFORMANCE_TEST_DIR
    if not root.is_dir():
        return []
    names: list[str] = []
    for path in sorted(root.rglob("*Test.java")):
        if "isolated" in path.relative_to(root).parts:
            continue
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        if "@Test" not in text:
            continue
        names.append(path.stem)
    return names


def run_capture(args: list[str], cwd: Path) -> tuple[int, str]:
    try:
        completed = subprocess.run(
            args,
            cwd=cwd,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
        )
        return completed.returncode, (completed.stdout or "") + (completed.stderr or "")
    except FileNotFoundError:
        return 1, f"command not found: {args[0]}"
    except OSError as exc:
        return 1, f"command failed: {args[0]}: {exc}"


def git_meta(repo_root: Path) -> dict[str, str]:
    na = {"commit": "n/a", "branch": "n/a", "dirty": "n/a"}
    if shutil.which("git") is None:
        return na
    code, commit = run_capture(["git", "rev-parse", "--short", "HEAD"], repo_root)
    if code != 0 or not commit.strip():
        return na
    _, branch = run_capture(["git", "rev-parse", "--abbrev-ref", "HEAD"], repo_root)
    _, status = run_capture(["git", "status", "--porcelain"], repo_root)
    return {
        "commit": commit.strip(),
        "branch": branch.strip() or "n/a",
        "dirty": "dirty" if status.strip() else "clean",
    }


def java_meta() -> str:
    java = shutil.which("java")
    if java is None:
        return "n/a"
    completed = subprocess.run(
        [java, "-version"],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    lines = (completed.stderr or completed.stdout or "").strip().splitlines()
    return " / ".join(line.strip() for line in lines if line.strip()) or "n/a"


def resolve_mvn() -> str | None:
    for name in ("mvn", "mvn.cmd", "mvn.bat"):
        path = shutil.which(name)
        if path:
            return path
    return None


def run_maven_streaming(repo_root: Path, cmd: list[str]) -> tuple[int, list[str]]:
    mvn = resolve_mvn()
    if mvn is None:
        return 1, ["Maven not found on PATH (mvn / mvn.cmd)."]
    full_cmd = [mvn, *cmd]
    use_shell = os.name == "nt" and mvn.lower().endswith((".cmd", ".bat"))
    raw_lines: list[str] = []
    try:
        process = subprocess.Popen(
            full_cmd if not use_shell else subprocess.list2cmdline(full_cmd),
            cwd=repo_root,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
            shell=use_shell,
        )
        assert process.stdout is not None
        for line in process.stdout:
            line = line.rstrip("\n\r")
            raw_lines.append(line)
            print(line, flush=True)
        return process.wait(), raw_lines
    except OSError as exc:
        return 1, [f"Maven invocation failed: {exc}"]


def extract_metric_message(line: str) -> str:
    match = LOG_MSG_RE.search(line)
    return match.group(1) if match else line


def md_cell(text: str) -> str:
    return text.replace("|", "\\|").replace("\n", " ").strip()


def split_metric_body(body: str) -> tuple[str, str]:
    if ":" in body:
        key, value_text = body.split(":", 1)
        return key.strip(), value_text.strip()
    return body, body


def _is_markdown_table_separator(cell: str) -> bool:
    return bool(cell) and set(cell) <= {"-", ":", " "}


def parse_primary_metric(value_text: str) -> tuple[str, float, bool] | None:
    for field_name, pattern, lower_is_better in PRIMARY_METRIC_PATTERNS:
        match = pattern.search(value_text)
        if match:
            try:
                return field_name, float(match.group(1)), lower_is_better
            except ValueError:
                continue
    # 兼容结果文件中的 "value (metric_field)" 写法（含 n=/min=/max= 后缀）
    stored = STORED_METRIC_RE.match(value_text.strip())
    if stored:
        field_name = stored.group(2)
        if field_name in STORED_FIELD_DIRECTION:
            try:
                return field_name, float(stored.group(1)), STORED_FIELD_DIRECTION[field_name]
            except ValueError:
                return None
    return None


def iter_metric_section_entries(text: str) -> list[tuple[str, str, str]]:
    in_section = False
    entries: list[tuple[str, str, str]] = []
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if line.startswith("## "):
            in_section = line == "## 基准指标"
            continue
        if not in_section:
            continue
        body = ""
        if line.startswith("- "):
            body = line[2:].strip()
        else:
            match = META_ROW_RE.match(line)
            if not match:
                continue
            col1, col2 = match.group(1).strip(), match.group(2).strip()
            if col1 == "指标" or _is_markdown_table_separator(col1):
                continue
            col1 = col1.replace("\\|", "|")
            col2 = col2.replace("\\|", "|")
            body = f"{col1}: {col2}" if col2 and col2 != "—" else col1
        if not body:
            continue
        key, value_text = split_metric_body(body)
        entries.append((key, value_text, body))
    return entries


def parse_metrics_from_result(path: Path) -> dict[str, ParsedMetric]:
    text = path.read_text(encoding="utf-8", errors="replace")
    metrics: dict[str, ParsedMetric] = {}
    for key, value_text, body in iter_metric_section_entries(text):
        parsed = parse_primary_metric(value_text)
        if parsed is None:
            # JMH 表可能直接是 "12.3 us/op" 整格
            parsed = parse_primary_metric(body)
        if parsed is None:
            continue
        field_name, value, lower_is_better = parsed
        sample_count = 1
        min_v = max_v = None
        range_match = re.search(
            r"n=(\d+).*?min=([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?).*?"
            r"max=([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)",
            value_text,
        )
        if range_match:
            sample_count = int(range_match.group(1))
            min_v = float(range_match.group(2))
            max_v = float(range_match.group(3))
        metrics.setdefault(
            key,
            ParsedMetric(
                key=key,
                value=value,
                metric_field=field_name,
                lower_is_better=lower_is_better,
                raw=body,
                sample_count=sample_count,
                min_value=min_v,
                max_value=max_v,
            ),
        )
    return metrics


def parse_meta_from_result(text: str) -> dict[str, str]:
    meta: dict[str, str] = {}
    for line in text.splitlines():
        match = META_ROW_RE.match(line.strip())
        if not match:
            continue
        key, value = match.group(1).strip(), match.group(2).strip()
        if key in {
            "开始时间",
            "结束时间",
            "耗时",
            "结果",
            "Git 分支",
            "Git commit",
            "Profile",
            "Repeat",
            "Label",
        }:
            meta[key] = value
    return meta


def parse_metrics_from_log_lines(raw_lines: list[str]) -> dict[str, ParsedMetric]:
    metrics: dict[str, ParsedMetric] = {}
    for line in raw_lines:
        if not METRIC_RE.search(line):
            continue
        body = extract_metric_message(line)
        key, value_text = split_metric_body(body)
        parsed = parse_primary_metric(value_text)
        if parsed is None:
            continue
        field_name, value, lower_is_better = parsed
        metrics.setdefault(
            key,
            ParsedMetric(
                key=key,
                value=value,
                metric_field=field_name,
                lower_is_better=lower_is_better,
                raw=body,
            ),
        )
    return metrics


def median(values: list[float]) -> float:
    return float(statistics.median(values))


def format_number(value: float) -> str:
    if abs(value) >= 1000:
        return f"{value:,.2f}".rstrip("0").rstrip(".")
    if abs(value) >= 1:
        return f"{value:.4g}"
    return f"{value:.6g}"


def is_core_metric_key(key: str) -> bool:
    return CORE_KEY_RE.search(key) is not None


def absolute_threshold(
    field_name: str,
    old: float,
    new: float,
    thresholds: NoiseThresholds,
) -> float:
    if field_name in {"avg_ms", "avg_round_ms", "total_ms", "jmh_ms"}:
        return thresholds.abs_ms
    if field_name in {"avg_us", "jmh_us"}:
        return thresholds.abs_us
    if field_name in {"avg_ns", "jmh_ns"}:
        return thresholds.abs_ns
    if field_name == "speedup":
        return thresholds.abs_speedup
    if field_name in {"throughput", "updates_per_sec"}:
        return thresholds.throughput_abs_rel_pct / 100.0 * max(abs(old), abs(new), 0.0)
    return thresholds.abs_ms


def direction_trend(old: float, new: float, lower_is_better: bool, significant: bool) -> str:
    if new == old:
        return "持平"
    better = (new < old) if lower_is_better else (new > old)
    if significant:
        return "显著变好" if better else "显著变差"
    return "变好" if better else "变差"


def classify_change(
    old_m: ParsedMetric,
    new_m: ParsedMetric,
    thresholds: NoiseThresholds,
) -> tuple[str, str]:
    if old_m.metric_field != new_m.metric_field:
        return "—", "字段不可比"
    if old_m.metric_field == "speedup" or "参考" in old_m.key or "对照" in old_m.key:
        if old_m.value == 0:
            return "n/a", "仅参考"
        pct = (new_m.value - old_m.value) / abs(old_m.value) * 100.0
        return f"{pct:+.2f}%", "仅参考"

    core = is_core_metric_key(old_m.key)
    noise_rel = thresholds.core_noise_rel_pct if core else thresholds.noise_rel_pct
    sig_rel = thresholds.core_sig_rel_pct if core else thresholds.sig_rel_pct

    old, new = old_m.value, new_m.value
    abs_diff = abs(new - old)
    tabs = absolute_threshold(old_m.metric_field, old, new, thresholds)

    if old == 0 and new == 0:
        return "0%", "持平"
    if old == 0:
        if abs_diff < tabs:
            return "n/a", "持平"
        return "n/a", direction_trend(old, new, new_m.lower_is_better, significant=True)

    pct = (new - old) / abs(old) * 100.0
    pct_text = f"{pct:+.2f}%"
    if abs(pct) < noise_rel or abs_diff < tabs:
        return pct_text, "持平"
    return pct_text, direction_trend(old, new, new_m.lower_is_better, abs(pct) >= sig_rel)


def labels_dir(output_dir: Path) -> Path:
    path = output_dir / LABELS_DIRNAME
    path.mkdir(parents=True, exist_ok=True)
    return path


def save_label(output_dir: Path, label: str, result_file: Path) -> Path:
    dest = labels_dir(output_dir) / f"{label}.md"
    shutil.copy2(result_file, dest)
    return dest


def maybe_save_label(
    output_dir: Path,
    label: str,
    result_file: Path,
    exit_code: int,
    metrics: dict[str, ParsedMetric],
) -> Path | None:
    """仅成功且解析到指标时写入基线，避免失败跑覆盖 labels/。"""
    if not label:
        return None
    if exit_code != 0 or not metrics:
        print(
            f"Skip label '{label}': exit={exit_code}, metrics={len(metrics)}",
            file=sys.stderr,
        )
        return None
    saved = save_label(output_dir, label, result_file)
    print(f"Label saved to: {saved}")
    return saved


def resolve_label_file(output_dir: Path, label: str) -> Path | None:
    path = labels_dir(output_dir) / f"{label}.md"
    return path if path.is_file() else None


def result_file_kind(path: Path) -> str:
    name = path.name
    if name.startswith("jmh-"):
        return "jmh"
    if name.startswith("batch-") or name.startswith("performance-"):
        return "full"
    meta = parse_meta_from_result(path.read_text(encoding="utf-8", errors="replace"))
    profile = meta.get("Profile", "").strip().lower()
    if profile == "core":
        return "jmh"
    if profile == "full":
        return "full"
    return "other"


def list_result_files(output_dir: Path, *, kind: str | None = None) -> list[Path]:
    files: list[Path] = []
    for pattern in ("performance-*.md", "batch-*.md", "jmh-*.md"):
        files.extend(output_dir.glob(pattern))
    if kind:
        files = [f for f in files if result_file_kind(f) == kind]
    return sorted(files, key=lambda f: f.stat().st_mtime)


def pick_compare_pair(output_dir: Path) -> tuple[Path, Path] | None:
    """取最近一份结果，并优先配对同类型（full/jmh）的上一份。"""
    files = list_result_files(output_dir)
    if len(files) < 2:
        return None
    newer = files[-1]
    kind = result_file_kind(newer)
    older_same = [f for f in files[:-1] if result_file_kind(f) == kind]
    if older_same:
        return older_same[-1], newer
    return files[-2], newer


def build_batch_markdown(
    *,
    profile: str,
    label: str,
    repeat: int,
    started_at: datetime,
    finished_at: datetime,
    exit_code: int,
    git: dict[str, str],
    java: str,
    repo_root: Path,
    metrics: dict[str, ParsedMetric],
    notes: list[str],
) -> str:
    duration_sec = round((finished_at - started_at).total_seconds(), 1)
    result_label = "PASSED" if exit_code == 0 else f"FAILED (exit {exit_code})"
    parts = [
        "# ECS 性能批次结果",
        "",
        "## 元信息",
        "",
        "| 项 | 值 |",
        "|---|---|",
        f"| 开始时间 | {started_at.strftime('%Y-%m-%d %H:%M:%S')} |",
        f"| 结束时间 | {finished_at.strftime('%Y-%m-%d %H:%M:%S')} |",
        f"| 耗时 | {duration_sec}s |",
        f"| 结果 | {result_label} |",
        f"| Profile | {profile} |",
        f"| Repeat | {repeat} |",
        f"| Label | {label or 'n/a'} |",
        f"| Git 分支 | {git['branch']} |",
        f"| Git commit | {git['commit']} ({git['dirty']}) |",
        f"| Java | {java} |",
        f"| 工作目录 | {repo_root} |",
        "",
        "## 基准指标",
        "",
        "| 指标 | 结果 |",
        "|---|---|",
    ]
    if metrics:
        for key in sorted(metrics):
            m = metrics[key]
            detail = f"{format_number(m.value)} ({m.metric_field})"
            if m.sample_count > 1 and m.min_value is not None and m.max_value is not None:
                detail += (
                    f", n={m.sample_count}, min={format_number(m.min_value)}, "
                    f"max={format_number(m.max_value)}"
                )
            parts.append(f"| {md_cell(key)} | {md_cell(detail)} |")
    else:
        parts.append("| _(无指标)_ | — |")
    if notes:
        parts.extend(["", "## 备注", ""])
        parts.extend(f"- {n}" for n in notes)
    parts.append("")
    return "\n".join(parts)


def build_compare_markdown(
    older: Path,
    newer: Path,
    older_meta: dict[str, str],
    newer_meta: dict[str, str],
    older_metrics: dict[str, ParsedMetric],
    newer_metrics: dict[str, ParsedMetric],
    thresholds: NoiseThresholds,
) -> str:
    all_keys = sorted(set(older_metrics) | set(newer_metrics))
    counts = {
        "显著变好": 0,
        "显著变差": 0,
        "变好": 0,
        "变差": 0,
        "持平": 0,
        "仅参考": 0,
        "字段不可比": 0,
        "仅上一批": 0,
        "仅最近一批": 0,
    }
    detail_rows: list[tuple[int, str]] = []
    for key in all_keys:
        old_m = older_metrics.get(key)
        new_m = newer_metrics.get(key)
        if old_m is None and new_m is not None:
            counts["仅最近一批"] += 1
            row = (
                f"| {key} | — | {format_number(new_m.value)} ({new_m.metric_field}) "
                f"| 新增 | 新增 |"
            )
            detail_rows.append((TREND_SORT_ORDER["新增"], row))
            continue
        if new_m is None and old_m is not None:
            counts["仅上一批"] += 1
            row = (
                f"| {key} | {format_number(old_m.value)} ({old_m.metric_field}) | — "
                f"| 缺失 | 缺失 |"
            )
            detail_rows.append((TREND_SORT_ORDER["缺失"], row))
            continue
        assert old_m is not None and new_m is not None
        pct_text, trend = classify_change(old_m, new_m, thresholds)
        counts[trend] = counts.get(trend, 0) + 1
        tier = "core" if is_core_metric_key(key) else "full"
        row = (
            f"| {key} | {format_number(old_m.value)} ({old_m.metric_field}) | "
            f"{format_number(new_m.value)} ({new_m.metric_field}) | {pct_text} | {trend} ({tier}) |"
        )
        detail_rows.append((TREND_SORT_ORDER.get(trend, 99), row))

    detail_rows.sort(key=lambda item: (item[0], item[1]))
    comparable = (
        counts["显著变好"]
        + counts["显著变差"]
        + counts["变好"]
        + counts["变差"]
        + counts["持平"]
    )
    parts = [
        "# ECS 性能测试结果对比",
        "",
        "## 对比范围",
        "",
        "| 项 | 基线 / 上一批 | 当前 / 最近一批 |",
        "|---|---|---|",
        f"| 文件 | `{older.name}` | `{newer.name}` |",
        f"| 开始时间 | {older_meta.get('开始时间', 'n/a')} | {newer_meta.get('开始时间', 'n/a')} |",
        f"| Profile | {older_meta.get('Profile', 'n/a')} | {newer_meta.get('Profile', 'n/a')} |",
        f"| Repeat | {older_meta.get('Repeat', 'n/a')} | {newer_meta.get('Repeat', 'n/a')} |",
        f"| Label | {older_meta.get('Label', 'n/a')} | {newer_meta.get('Label', 'n/a')} |",
        f"| 结果 | {older_meta.get('结果', 'n/a')} | {newer_meta.get('结果', 'n/a')} |",
        f"| Git commit | {older_meta.get('Git commit', 'n/a')} | {newer_meta.get('Git commit', 'n/a')} |",
        "",
        "## 噪声门槛",
        "",
        f"- full 指标: `|Δ| < {thresholds.noise_rel_pct:g}%` 持平；"
        f"`|Δ| ≥ {thresholds.sig_rel_pct:g}%` 显著",
        f"- core/JMH 指标: `|Δ| < {thresholds.core_noise_rel_pct:g}%` 持平；"
        f"`|Δ| ≥ {thresholds.core_sig_rel_pct:g}%` 显著",
        "- 需同时超过相对门槛与绝对门槛才记变好/变差",
        "",
        "## 汇总",
        "",
        f"- 可比对指标: {comparable}",
        f"- 显著变差: {counts['显著变差']}",
        f"- 变差: {counts['变差']}",
        f"- 显著变好: {counts['显著变好']}",
        f"- 变好: {counts['变好']}",
        f"- 持平（噪声内）: {counts['持平']}",
        f"- 仅参考: {counts['仅参考']}",
        f"- 字段不可比: {counts['字段不可比']}",
        f"- 仅基线: {counts['仅上一批']}",
        f"- 仅当前: {counts['仅最近一批']}",
        "",
        "## 明细",
        "",
        "| 指标 | 基线 | 当前 | 变化 | 趋势 |",
        "|---|---|---|---|---|",
    ]
    if detail_rows:
        parts.extend(row for _, row in detail_rows)
    else:
        parts.append("| _(无可用基准指标)_ | — | — | — | — |")
    parts.extend(
        [
            "",
            "_说明: 延迟类下降为变好；吞吐上升为变好。"
            "core 结论以 JMH `--profile core` 为准；full 为广覆盖回归。"
            "噪声门槛不是统计显著性检验。_",
            "",
        ]
    )
    return "\n".join(parts)


def write_compare(
    older: Path,
    newer: Path,
    output_dir: Path,
    thresholds: NoiseThresholds,
    name_hint: str = "",
) -> Path:
    older_text = older.read_text(encoding="utf-8", errors="replace")
    newer_text = newer.read_text(encoding="utf-8", errors="replace")
    markdown = build_compare_markdown(
        older,
        newer,
        parse_meta_from_result(older_text),
        parse_meta_from_result(newer_text),
        parse_metrics_from_result(older),
        parse_metrics_from_result(newer),
        thresholds,
    )
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    suffix = f"-{name_hint}" if name_hint else ""
    compare_file = output_dir / f"compare{suffix}-{timestamp}.md"
    compare_file.write_text(markdown, encoding="utf-8")
    print(markdown)
    print()
    print(f"Compare report written to: {compare_file}")
    return compare_file


def aggregate_metrics(
    samples: list[dict[str, ParsedMetric]],
) -> dict[str, ParsedMetric]:
    acc: dict[str, MetricAccumulator] = {}
    for sample in samples:
        for key, metric in sample.items():
            bucket = acc.setdefault(key, MetricAccumulator())
            if not bucket.metric_field:
                bucket.metric_field = metric.metric_field
                bucket.lower_is_better = metric.lower_is_better
            if bucket.metric_field != metric.metric_field:
                continue
            bucket.values.append(metric.value)
            bucket.raw_samples.append(metric.raw)
    result: dict[str, ParsedMetric] = {}
    for key, bucket in acc.items():
        if not bucket.values:
            continue
        med = median(bucket.values)
        result[key] = ParsedMetric(
            key=key,
            value=med,
            metric_field=bucket.metric_field,
            lower_is_better=bucket.lower_is_better,
            raw=f"{format_number(med)} ({bucket.metric_field})",
            sample_count=len(bucket.values),
            min_value=min(bucket.values),
            max_value=max(bucket.values),
        )
    return result


def parse_jmh_json(path: Path) -> dict[str, ParsedMetric]:
    data = json.loads(path.read_text(encoding="utf-8"))
    metrics: dict[str, ParsedMetric] = {}
    if not isinstance(data, list):
        return metrics
    for item in data:
        if not isinstance(item, dict):
            continue
        bench = str(item.get("benchmark", ""))
        short = bench.rsplit(".", 1)[-1]
        class_name = bench.rsplit(".", 2)[-2] if bench.count(".") >= 2 else bench
        params = item.get("params") or {}
        param_text = ",".join(f"{k}={v}" for k, v in sorted(params.items()))
        key = f"{class_name}.{short}"
        if param_text:
            key = f"{key} ({param_text})"
        primary = item.get("primaryMetric") or {}
        score = primary.get("score")
        unit = str(primary.get("scoreUnit", ""))
        if score is None:
            continue
        score_f = float(score)
        if "us/op" in unit:
            field_name, lower = "jmh_us", True
        elif "ns/op" in unit:
            field_name, lower = "jmh_ns", True
        elif "ms/op" in unit:
            field_name, lower = "jmh_ms", True
        elif "ops/" in unit:
            field_name, lower = "throughput", False
        else:
            field_name, lower = "jmh_us", True
        raw = f"{format_number(score_f)} {unit}".strip()
        metrics[key] = ParsedMetric(
            key=key,
            value=score_f,
            metric_field=field_name,
            lower_is_better=lower,
            raw=raw,
        )
    return metrics


def run_full_profile(
    repo_root: Path,
    output_dir: Path,
    repeat: int,
    label: str,
) -> tuple[int, Path]:
    discovered = discover_performance_tests(repo_root)
    if not discovered:
        print(f"No performance tests under {repo_root / PERFORMANCE_TEST_DIR}", file=sys.stderr)
        return 1, Path()
    test_filter = ",".join(discovered)
    git = git_meta(repo_root)
    java = java_meta()
    started_at = datetime.now()
    print(f"Running full JUnit performance suite via -Pperf (repeat={repeat})...")
    for name in discovered:
        print(f"  - {name}")

    samples: list[dict[str, ParsedMetric]] = []
    exit_code = 0
    notes: list[str] = []
    for i in range(1, repeat + 1):
        print()
        print(f"===== JVM fork {i}/{repeat} =====")
        code, lines = run_maven_streaming(
            repo_root,
            ["-Pperf", "test", f"-Dtest={test_filter}"],
        )
        if code != 0:
            exit_code = code
            notes.append(f"fork {i} failed with exit {code}")
        metrics = parse_metrics_from_log_lines(lines)
        if not metrics:
            notes.append(f"fork {i}: no metrics parsed")
        samples.append(metrics)

    aggregated = aggregate_metrics(samples)
    finished_at = datetime.now()
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    label_part = f"{label}-" if label else ""
    result_file = output_dir / f"batch-{label_part}{timestamp}.md"
    markdown = build_batch_markdown(
        profile="full",
        label=label,
        repeat=repeat,
        started_at=started_at,
        finished_at=finished_at,
        exit_code=exit_code,
        git=git,
        java=java,
        repo_root=repo_root,
        metrics=aggregated,
        notes=notes
        + [
            "经 `mvn -Pperf` 运行；默认 `mvn test` 不包含本套件。",
            f"每项主值为 {repeat} 次独立 JVM 的中位数。",
        ],
    )
    result_file.write_text(markdown, encoding="utf-8")
    print()
    print(f"Batch results written to: {result_file}")
    maybe_save_label(output_dir, label, result_file, exit_code, aggregated)
    return exit_code, result_file


def run_core_jmh(
    repo_root: Path,
    output_dir: Path,
    label: str,
) -> tuple[int, Path]:
    git = git_meta(repo_root)
    java = java_meta()
    started_at = datetime.now()
    print("Running core JMH suite via -Pjmh (world.update hot path)...")
    jmh_json = repo_root / JMH_RESULT_JSON
    if jmh_json.exists():
        jmh_json.unlink()

    # test-compile 生成 JMH 合成类，再 exec:exec 运行（独立 classpath，fork 才能找到 ForkedMain）
    code_compile, lines_compile = run_maven_streaming(
        repo_root,
        ["-Pjmh", "test-compile"],
    )
    if code_compile != 0:
        print("JMH test-compile failed.", file=sys.stderr)
        finished_at = datetime.now()
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        label_part = f"{label}-" if label else ""
        result_file = output_dir / f"jmh-{label_part}{timestamp}.md"
        result_file.write_text(
            build_batch_markdown(
                profile="core",
                label=label,
                repeat=1,
                started_at=started_at,
                finished_at=finished_at,
                exit_code=code_compile,
                git=git,
                java=java,
                repo_root=repo_root,
                metrics={},
                notes=["JMH test-compile failed"] + lines_compile[-20:],
            ),
            encoding="utf-8",
        )
        maybe_save_label(output_dir, label, result_file, code_compile, {})
        return code_compile, result_file

    code_run, lines_run = run_maven_streaming(
        repo_root,
        [
            "-Pjmh",
            "exec:exec",
            f"-Djmh.benchmarks={os.environ.get('JMH_BENCHMARKS', 'top.kgame.lib.ecsjmh')}",
            f"-Djmh.fork={os.environ.get('JMH_FORK', '3')}",
            f"-Djmh.warmupIterations={os.environ.get('JMH_WI', '3')}",
            f"-Djmh.measurementIterations={os.environ.get('JMH_I', '5')}",
        ],
    )
    finished_at = datetime.now()
    metrics: dict[str, ParsedMetric] = {}
    notes = [
        "经 `mvn -Pjmh test-compile exec:exec` 运行；不经过 surefire。",
        "核心结论门槛约 5%/10%；与 full JUnit 套件分开解读。",
    ]
    if jmh_json.is_file():
        metrics = parse_jmh_json(jmh_json)
        notes.append(f"JMH JSON: {jmh_json}")
    else:
        notes.append("未找到 target/jmh-result.json，尝试从日志提取失败")
        code_run = code_run or 1

    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    label_part = f"{label}-" if label else ""
    result_file = output_dir / f"jmh-{label_part}{timestamp}.md"
    result_file.write_text(
        build_batch_markdown(
            profile="core",
            label=label,
            repeat=1,
            started_at=started_at,
            finished_at=finished_at,
            exit_code=code_run,
            git=git,
            java=java,
            repo_root=repo_root,
            metrics=metrics,
            notes=notes,
        ),
        encoding="utf-8",
    )
    print()
    print(f"JMH results written to: {result_file}")
    maybe_save_label(output_dir, label, result_file, code_run, metrics)
    return code_run, result_file


def resolve_output_dir(repo_root: Path, output_dir_arg: str) -> Path:
    if output_dir_arg.strip():
        return Path(output_dir_arg).expanduser().resolve()
    return repo_root / "perf-results"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "性能影响验证：full=JUnit 多 JVM 中位数；core=JMH world.update。"
            "默认 mvn test 不含压测，请用本脚本或 -Pperf/-Pjmh。"
        ),
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  # 改代码前
  python scripts/run_performance_tests.py --profile full --repeat 5 --label before
  python scripts/run_performance_tests.py --profile core --label before-core

  # 改代码后
  python scripts/run_performance_tests.py --profile full --repeat 5 --label after --diff before
  python scripts/run_performance_tests.py --profile core --label after-core --diff before-core

  # 仅对比最近两次结果文件
  python scripts/run_performance_tests.py --compare
""",
    )
    parser.add_argument("--output-dir", default="", help="结果目录（默认: <repo>/perf-results）")
    parser.add_argument(
        "--profile",
        choices=("full", "core"),
        default="full",
        help="full=JUnit 广覆盖；core=JMH 核心 world.update",
    )
    parser.add_argument(
        "--repeat",
        type=int,
        default=5,
        help="full 模式下独立 JVM 次数（默认 5）；core/JMH 忽略此参数",
    )
    parser.add_argument("--label", default="", help="保存/覆盖 labels/<label>.md 基线")
    parser.add_argument(
        "--diff",
        default="",
        help="与 labels/<name>.md 基线对比（跑完当前批次后，或可配合 --compare）",
    )
    parser.add_argument(
        "--compare",
        action="store_true",
        help="不跑测试；按修改时间取最近结果，并优先与同类型（full/jmh）上一份对比",
    )
    parser.add_argument("--noise-rel", type=float, default=DEFAULT_NOISE_REL_PCT)
    parser.add_argument("--sig-rel", type=float, default=DEFAULT_SIG_REL_PCT)
    parser.add_argument("--core-noise-rel", type=float, default=CORE_NOISE_REL_PCT)
    parser.add_argument("--core-sig-rel", type=float, default=CORE_SIG_REL_PCT)
    parser.add_argument("--noise-abs-ms", type=float, default=DEFAULT_ABS_MS)
    parser.add_argument("--noise-abs-us", type=float, default=DEFAULT_ABS_US)
    parser.add_argument("--noise-abs-ns", type=float, default=DEFAULT_ABS_NS)
    parser.add_argument("--noise-abs-speedup", type=float, default=DEFAULT_ABS_SPEEDUP)
    parser.add_argument(
        "--throughput-abs-rel",
        type=float,
        default=DEFAULT_THROUGHPUT_ABS_REL_PCT,
    )
    args = parser.parse_args(argv)

    if args.repeat < 1:
        print("--repeat must be >= 1", file=sys.stderr)
        return 2
    if args.noise_rel < 0 or args.sig_rel < 0 or args.core_noise_rel < 0 or args.core_sig_rel < 0:
        print("noise/sig thresholds must be >= 0", file=sys.stderr)
        return 2
    if args.sig_rel < args.noise_rel or args.core_sig_rel < args.core_noise_rel:
        print("sig thresholds must be >= corresponding noise thresholds", file=sys.stderr)
        return 2

    repo_root = Path(__file__).resolve().parent.parent
    output_dir = resolve_output_dir(repo_root, args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    thresholds = NoiseThresholds(
        noise_rel_pct=args.noise_rel,
        sig_rel_pct=args.sig_rel,
        abs_ms=args.noise_abs_ms,
        abs_us=args.noise_abs_us,
        abs_ns=args.noise_abs_ns,
        abs_speedup=args.noise_abs_speedup,
        throughput_abs_rel_pct=args.throughput_abs_rel,
        core_noise_rel_pct=args.core_noise_rel,
        core_sig_rel_pct=args.core_sig_rel,
    )

    if args.compare and not args.diff:
        if not output_dir.is_dir():
            print(f"Output directory not found: {output_dir}", file=sys.stderr)
            return 1
        pair = pick_compare_pair(output_dir)
        if pair is None:
            print(f"Need at least 2 result files in {output_dir}.", file=sys.stderr)
            return 1
        older, newer = pair
        write_compare(older, newer, output_dir, thresholds)
        return 0

    if args.compare and args.diff:
        baseline = resolve_label_file(output_dir, args.diff)
        if baseline is None:
            print(f"Label not found: {args.diff}", file=sys.stderr)
            return 1
        kind = result_file_kind(baseline)
        files = list_result_files(output_dir, kind=kind if kind != "other" else None)
        if not files:
            print("No current result file to compare.", file=sys.stderr)
            return 1
        write_compare(baseline, files[-1], output_dir, thresholds, name_hint=f"{args.diff}-vs-latest")
        return 0

    if args.profile == "core":
        exit_code, result_file = run_core_jmh(repo_root, output_dir, args.label)
    else:
        exit_code, result_file = run_full_profile(
            repo_root, output_dir, args.repeat, args.label
        )

    if args.diff and result_file and result_file.is_file():
        baseline = resolve_label_file(output_dir, args.diff)
        if baseline is None:
            print(f"Label not found for --diff: {args.diff}", file=sys.stderr)
            return 1
        write_compare(
            baseline,
            result_file,
            output_dir,
            thresholds,
            name_hint=f"{args.diff}-vs-{args.label or 'current'}",
        )

    return exit_code


if __name__ == "__main__":
    sys.exit(main())
