#!/usr/bin/env python3
"""Remove repeated entries from Release Please notes.

Stacked pull requests are merged with merge commits, so a change often reaches `main` twice: once
as the original commit and once through the PR that carried it. Release Please lists both. This
filter keeps one bullet per subject in each section, preferring the bullet that links its pull
request, and leaves every other line untouched.

Usage: dedupe_release_notes.py < notes.md > deduped.md
"""

import re
import sys

BULLET = re.compile(r"^\* (?P<subject>.+?) \(\[")
PULL_REQUEST_LINK = re.compile(r"\(\[#\d+\]\(")


def dedupe(notes: str) -> str:
    output: list[str] = []
    seen: dict[str, int] = {}
    for line in notes.splitlines():
        if line.startswith("#"):
            seen = {}
            output.append(line)
            continue
        match = BULLET.match(line)
        if not match:
            output.append(line)
            continue
        subject = match.group("subject")
        if subject not in seen:
            seen[subject] = len(output)
            output.append(line)
        elif PULL_REQUEST_LINK.search(line) and not PULL_REQUEST_LINK.search(output[seen[subject]]):
            output[seen[subject]] = line
    trailing_newline = "\n" if notes.endswith("\n") else ""
    return "\n".join(output) + trailing_newline


if __name__ == "__main__":
    sys.stdout.write(dedupe(sys.stdin.read()))
