# Oracle pool (v1b)

## Difference from v1
- Using context variable for supplying index of each ballot box in the update transaction (experimental)
- Preserve the Erg amount in ballot boxes (by using == instead of >=)
- Each box can have exactly 1 vote
- Ensure that ballots output in an update tx cannot be used as a valid ballot by requiring R5 of the new box to be undefined

