#!/usr/bin/env python3
"""Regression coverage for Apple's non-localized Review Notes limit."""
import contextlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location(
    "appstore_content", Path(__file__).with_name("check-appstore-content.py")
)
content = importlib.util.module_from_spec(spec)
spec.loader.exec_module(content)


class ReviewNotesTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.path = Path(self.directory.name) / "notes.txt"

    def rejected(self):
        with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
            content.validate_review_notes(self.path)

    def test_accepts_4000_characters_including_non_ascii(self):
        self.path.write_text("审" * 4000 + "\n", encoding="utf-8")
        content.validate_review_notes(self.path)

    def test_rejects_4001_characters(self):
        self.path.write_text("a" * 4001, encoding="utf-8")
        self.rejected()

    def test_rejects_missing_notes(self):
        self.rejected()

    def test_rejects_empty_notes(self):
        self.path.write_text(" \n", encoding="utf-8")
        self.rejected()


if __name__ == "__main__":
    unittest.main()
