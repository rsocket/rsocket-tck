# encoding utf-8

Feature: Connection establishment

  Scenario: Successful connection establishment
    Given server listening to a port
    When  client sends SETUP frame with
      | metadata-flag      | 0                                       |
      | resume-flag        | 0                                       |
      | lease-flag         | 0                                       |
      | major-version      | 1                                       |
      | minor-version      | 0                                       |
      | mime-type-metadata | message/x.rsocket.composite-metadata.v0 |
      | keep-alive         | 500                                     |
      | max-life-time      | 1500                                    |
      | mime-type-data     | text/plain                              |
      | metadata           | null                                    |
      | data               | null                                    |
    Then server requires the following SETUP frame content
      | stream-id            | 0                                       | 0b0000_0000_0000_0000_0000_0000_0000_0000                                                                              |
      | frame-type           | 1                                       | 0b0000_01                                                                                                              |
      | flags                | 0                                       | 0b00_0000_0000                                                                                                         |
      | major-version        | 1                                       | 0b0000_0000_0000_0001                                                                                                  |
      | minor-version        | 0                                       | 0b0000_0000_0000_0000                                                                                                  |
      | keep-alive           | 500                                     | 0b0000_0000_0000_0000_0000_0001_1111_0100                                                                              |
      | max-life-time        | 1500                                    | 0b0000_0000_0000_0000_0000_0101_1101_1100                                                                              |
      | mime-length-metadata | 39                                      | 0b0010_0111                                                                                                            |
      | mime-type-metadata   | message/x.rsocket.composite-metadata.v0 | 0x6d_65_73_73_61_67_65_2f_78_2e_72_73_6f_63_6b_65_74_2e_63_6f_6d_70_6f_73_69_74_65_2d_6d_65_74_61_64_61_74_61_2e_76_30 |
      | mime-length-data     | 10                                      | 0b0000_1010                                                                                                            |
      | mime-type-data       | text/plain                              | 0x74_65_78_74_2f_70_6c_61_69_6e_0a                                                                                     |
    And connection is not closed after
