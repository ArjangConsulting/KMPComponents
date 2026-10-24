import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "skills/use-kmpcomponents/references/catalog.md"
REPOSITORY = "https://github.com/ArjangConsulting/KMPComponents/blob/main/"
FUNCTION = re.compile(
    r"\bpublic\s+(?:suspend\s+)?fun\s+(?:<[^>]+>\s*)?"
    r"(?:[\w<>?.]+\.)?([A-Za-z_]\w*)\s*\("
)
DECLARATION = re.compile(
    r"\bpublic\s+(?:(?:data|enum|sealed|value|annotation)\s+)*"
    r"(?:class|interface|object|val|var)\s+([A-Za-z_]\w*)"
)


def declaration_sources():
    sources = {}
    for module in ("components", "components-image"):
        for path in sorted((ROOT / module / "src/commonMain/kotlin").rglob("*.kt")):
            content = path.read_text()
            for name in set(FUNCTION.findall(content) + DECLARATION.findall(content)):
                sources.setdefault(name, set()).add(path.relative_to(ROOT).as_posix())
    return sources


def catalog():
    readme = (ROOT / "README.md").read_text()
    section = readme.split("\n## Catalog\n", 1)[1].split("\n### Names shared", 1)[0]
    sources = declaration_sources()
    lines = [
        "# Component catalog for agents",
        "",
        "Generated from the README catalog and public declaration locations. Maintainers regenerate",
        "it in the library checkout with `python3 scripts/update_agent_catalog.py` after updating",
        "those sources. Consuming applications do not need the generator.",
        "",
        "This index describes the checkout that produced it. Source links point to `main` for",
        "discovery; inspect a tag or source artifact matching the application's resolved dependency",
        "version before copying a signature. A name in this index does not prove it exists in an",
        "older release. Core imports use `io.github.maniramezan.kmpcomponents`; the image module",
        "uses `io.github.maniramezan.kmpcomponents.image` and does not target Wasm.",
    ]
    count = 0
    for line in section.splitlines():
        if line.startswith("### "):
            lines.extend(["", line[1:], "", "| Components | Purpose | Source |", "| --- | --- | --- |"])
        if not line.startswith("| `"):
            continue
        names, purpose = [part.strip() for part in line.strip("|").split("|", 1)]
        locations = set()
        for name in re.findall(r"`([^`]+)`", names):
            symbol = name.rsplit(".", 1)[-1]
            owners = sources.get(symbol, set())
            if len(owners) != 1:
                raise ValueError(f"Catalog symbol {name} needs one declaration source, found {sorted(owners)}")
            locations.update(owners)
        links = ", ".join(f"[{Path(path).name}]({REPOSITORY}{path})" for path in sorted(locations))
        lines.append(f"| {names} | {purpose} | {links} |")
        count += 1
    if count == 0:
        raise ValueError("README catalog contains no component rows")
    return "\n".join(lines) + "\n", count


def main():
    parser = argparse.ArgumentParser(description="Generate the consumer skill's component index")
    parser.add_argument("--check", action="store_true", help="Fail when the checked-in index is stale")
    arguments = parser.parse_args()
    content, count = catalog()
    if arguments.check:
        if not OUTPUT.exists() or OUTPUT.read_text() != content:
            parser.exit(1, "Agent catalog is stale. Run python3 scripts/update_agent_catalog.py\n")
        print(f"Agent catalog is current: {count} component entries")
    else:
        OUTPUT.parent.mkdir(parents=True, exist_ok=True)
        OUTPUT.write_text(content)
        print(f"Generated {OUTPUT.relative_to(ROOT)}: {count} component entries")


if __name__ == "__main__":
    main()
