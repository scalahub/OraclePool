# OraclePool v2.0a

### Differences from v2.0

- Reward tokens are stored in Pool box instead of Refresh box.
- Refresh contract is modified to work with reward tokens stored in pool box.
- An update may or may not preserve reward tokens. This depends on the voters.
- Ballot boxes have 2 additional registers representing values of the new pool box.
  - R7 contains the reward token id.
  - R8 contains the reward token amount.
- Update contract is modified to check the above two additional registers during update. 
