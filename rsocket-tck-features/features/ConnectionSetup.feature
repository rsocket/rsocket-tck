# encoding utf-8

Feature: Connection establishment

  Scenario: Successful connection establishment
    Given server listening to a port
    When  client sends SETUP frame with
      | metadata-flag  | 0                                       |
      | resume-flag    | 0                                       |
      | lease-flag     | 0                                       |
      | major-version  | 1                                       |
      | minor-version  | 0                                       |
      | mime-type-data | message/x.rsocket.composite-metadata.v0 |
      | keep-alive     | 500                                     |
      | max-life-time  | 500                                     |
    And server requires the following SETUP frame content
      | stream-id     | 0    |
      | frame-type    | 0x01 |
      | major-version | 1    |
      | minor-version | 0    |
    Then  server does not close connection after
