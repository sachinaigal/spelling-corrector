# spelling-corrector
A simple spelling corrector based on [Peter Norvig's blog](http://norvig.com/spell-correct.html).

##### Output:
```
{'bad': 70, 'bias': None, 'unknown': 15, 'secs': 1, 'pct': 74, 'n': 270}
{'bad': 131, 'bias': None, 'unknown': 43, 'secs': 2, 'pct': 67, 'n': 400}
```

Note - the result is slightly worse than Norvig's output:
```
{'bad': 68, 'bias': None, 'unknown': 15, 'secs': 16, 'pct': 74, 'n': 270}
{'bad': 130, 'bias': None, 'unknown': 43, 'secs': 26, 'pct': 67, 'n': 400}
```

I think this may be due to differing word counts (NWORDS) in some cases as is evident from his examples:

| Mine | Norvig's |
| -----|--------- |
| 'they' (3939) | 'they' (4939) |
| 'set' (326) | 'set' (325) |
| 'latter' (130) | 'latter' (11) |
| 'later' (335) | 'later' (116) |
| 'where' (978) | 'where' (123) |
| 'were' (4290) | 'were' (452) |
| 'the' (80031) | 'the' (81031) |
| 'there' (2973) | 'there' (4973) |
| 'their' (2956) | 'their' (3956) |
