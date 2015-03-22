# spelling-corrector
A simple spelling corrector based on [Peter Norvig's blog](http://norvig.com/spell-correct.html).

##### Output:
```
{'bad': 68, 'bias': None, 'unknown': 15, 'secs': 2, 'pct': 74, 'n': 270}
{'bad': 130, 'bias': None, 'unknown': 43, 'secs': 3, 'pct': 67, 'n': 400}
```

Although the test results are same, I noticed differing word counts (NWORDS) in some cases as is evident from his examples:

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

Scripts to confirm that my counts are correct:
```
export words="they\|set\|latter\|later\|where\|were\|the\|there\|their"
grep -i $words big.txt | tr -s ' ' '\n' | tr -s [:punct:] '\n' | tr [:upper:] [:lower:] | grep -w $words | sort | uniq -c | awk '{print $2,$1}'
they 3938
set 325
latter 129
later 334
where 977
were 4289
the 80020
the 10
there 2972
their 2955
```
*The NWORDS counts are greater by 1 due to 'smoothing'
