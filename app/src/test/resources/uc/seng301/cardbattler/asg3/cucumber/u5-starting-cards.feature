Feature: U5 As Alex, I want to place my 5 starting cards on the board so that I can start a fight.

  Scenario: AC1 - At the start of the game, I draw the first 5 cards of my battle deck.
    Given I am a player "name"
    And I have a battle deck named "stuff"
    When I draw a hand with "A" monsters
    Then I draw the first 5 cards of my deck

  Scenario: AC2 - Each card type must be placed at its dedicated place on the board.
    Given I am a player "name"
    And I have a battle deck named "stuff"
    When I draw a hand with "A" monsters
    Then The 5 cards went to the correct positions

  Scenario Outline: AC3 - When a monster card is placed in attack or defence mode, then its starting life is equal to its attack or
  defence respectively
    Given I am a player "name"
    And I have a battle deck named "stuff"
    When I draw a hand with <type> monsters, Has monster in hand
    Then The card has correct life for <type>
    Examples:
     | type |
     | "A"  |
     | "D"  |
