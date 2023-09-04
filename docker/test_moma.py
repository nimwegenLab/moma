import unittest
from pathlib import Path


class MyTestCase(unittest.TestCase):
    def test__get_top_level_paths__returns_deduplicated_list_of_parent_paths(self):
        from moma import get_top_level_paths
        paths = ["/folder1/",
                 "/folder1/subfolder/",
                 "/folder1/subfolder/",
                 "/folder1/anotherfolder/",
                 "/folder2/subfolder/",
                 "/folder2/anotherfolder/",
                 "/folder3/anotherfolder/",
                 "/folder3/anotherfolder/foo",
                 "/folder3/anotherfolder/bar",
                 "/folder3/anotherfolder/bar/folder1",
                 "/folder3/anotherfolder/bar/folder2",
                 "/folder3/anotherfolder/bar/folder3",]
        paths = [Path(p) for p in paths]
        res = get_top_level_paths(paths)
        expected = [Path('/folder1'), Path('/folder2/subfolder'), Path('/folder3/anotherfolder'), Path('/folder2/anotherfolder')]
        expected.sort()
        self.assertEqual(expected, res)


if __name__ == '__main__':
    unittest.main()
