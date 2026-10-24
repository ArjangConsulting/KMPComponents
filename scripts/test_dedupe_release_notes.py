import unittest

from dedupe_release_notes import dedupe

URL = "https://github.com/ArjangConsulting/KMPComponents"


class DedupeReleaseNotesTest(unittest.TestCase):
    def test_keeps_one_bullet_per_subject(self) -> None:
        notes = (
            "### Features\n\n"
            f"* add SegmentedContent ([6c4c562]({URL}/commit/6c4c562))\n"
            f"* add SegmentedContent ([58f175d]({URL}/commit/58f175d))\n"
            f"* add pills ([61ec86d]({URL}/commit/61ec86d))\n"
        )
        self.assertEqual(
            dedupe(notes),
            "### Features\n\n"
            f"* add SegmentedContent ([6c4c562]({URL}/commit/6c4c562))\n"
            f"* add pills ([61ec86d]({URL}/commit/61ec86d))\n",
        )

    def test_prefers_the_bullet_that_links_its_pull_request(self) -> None:
        notes = (
            "### Features\n\n"
            f"* add radio group ([26f1921]({URL}/commit/26f1921))\n"
            f"* add radio group ([#27]({URL}/issues/27)) ([78e5bf4]({URL}/commit/78e5bf4))\n"
        )
        self.assertEqual(
            dedupe(notes),
            "### Features\n\n"
            f"* add radio group ([#27]({URL}/issues/27)) ([78e5bf4]({URL}/commit/78e5bf4))\n",
        )

    def test_sections_are_deduplicated_independently(self) -> None:
        notes = (
            "### Features\n\n"
            f"* tidy labels ([1111111]({URL}/commit/1111111))\n\n"
            "### Bug Fixes\n\n"
            f"* tidy labels ([2222222]({URL}/commit/2222222))\n"
        )
        self.assertEqual(dedupe(notes), notes)

    def test_leaves_other_lines_untouched(self) -> None:
        notes = "## What's Changed\n* feat: x by @someone in #1\n* feat: x by @someone in #1\n"
        self.assertEqual(dedupe(notes), notes)


if __name__ == "__main__":
    unittest.main()
