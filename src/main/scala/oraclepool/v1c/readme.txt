# Oracle pool (v1c)

## Differences from v1b
- Use ballot boxes as data inputs in update tx, instead of inputs
- Not using dummy R5 as Int in ballot boxes anymore because of above
- Prevent reusing same data inputs in a blocks via chained tx using creationInfo (perpetual token approach)
- Prevent repetition of data inputs using ordering by box id. To use same in collection tx as well