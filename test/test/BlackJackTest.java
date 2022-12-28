package test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import static test.BlackJackTest.Card.*;
import static test.BlackJackTest.Game.playerWinsCrupier;

import org.junit.Test;

public class BlackJackTest {
    
    @Test
    public void given_one_card_should_calculate_value() {
        assertEquals(5, createHand(_5).value());
        assertEquals(6, createHand(_6).value());
        assertEquals(10, createHand(Jack).value());
        assertEquals(10, createHand(Queen).value());
        assertEquals(10, createHand(King).value());
        assertEquals(11, createHand(Ace).value());
    }
    
    @Test
    public void given_two_cards_should_calculate_value() {
        assertEquals(11, createHand(_5, _6).value());        
        assertEquals(12, createHand(Ace, Ace).value());        
    }
    
    @Test 
    public void given_two_cards_should_determine_if_is_black_jack() {
        assertEquals(false, createHand(_5, _6).isBlackJack());        
        assertEquals(true, createHand(Ace, Queen).isBlackJack());               
    }

    @Test 
    public void given_three_cards_should_determine_that_is_not_black_jack() {
        assertEquals(false, createHand(_5, _6, Queen).isBlackJack());               
    }
    
    @Test 
    public void given_two_cards_should_determine_that_is_not_bust() {
        assertEquals(false, createHand(_4,_3).isBust());               
    }
    
    @Test 
    public void given_three_cards_should_determine_if_is_bust() {
        assertEquals(true, createHand(_4, Jack, King).isBust());               
        assertEquals(false, createHand(_4, _2, _3).isBust());               
    }
    
    @Test 
    public void given_one_player_should_determine_if_his_hand_is_bust() {
        assertEquals(true, createPlayer("Player1",createHand(_4,Jack,King)).hand().isBust());
        assertEquals(false, createPlayer("Player1",createHand(_3,_4,Ace,_8)).hand().isBust());
    }
    
    @Test
    public void given_one_deck_and_taking_one_card_should_take_the_first_card() {
        assertEquals(Ace, createDeck(Ace,_3,_3,_9,Jack).takeCard());
        assertEquals(Queen, createDeck(Queen,_2,Ace,King,King,_8).takeCard());
    }
    
    @Test
    public void given_one_deck_and_taking_two_cards_should_take_the_first_two_cards() {
        Deck deck = createDeck(Queen,_2,Ace,King,King,_8);
        assertEquals(Queen, deck.takeCard());
        assertEquals(_2, deck.takeCard());
    }

    @Test
    public void given_deck_and_crupier_with_less_than_17_should_take_cards_from_deck() {
        Crupier crupier = createCrupier(createHand(_5,_3),createDeck(_2,Queen));
        assertEquals(20, crupier.hand().value());
    }

    @Test
    public void given_one_player_and_crupier_should_determine_if_player_wins_crupier() {
        assertEquals(true, playerWinsCrupier(createCrupier(createHand(_10,_6,_5), createDeck()), createPlayer("Player",createHand(Ace,Jack))));
        assertEquals(false, playerWinsCrupier(createCrupier(createHand(_9,_10), createDeck()), createPlayer("Player",createHand(_9,_8))));
    }
    
    @Test
    public void given_one_player_deck_and_crupier_with_less_than_17_should_determine_winners() {
        assertEquals("[]", createGame(createCrupier(createHand(_5,_3), createDeck(_2,Queen)),
                createPlayer("Player1",createHand(_2,_4,_2,Ace))).getWinners().toString());
    }
    
    @Test
    public void given_three_players_crupier_and_deck_should_determine_winners() {
        assertEquals("[Player1]", createGame(createCrupier(createHand(_6,Jack,_4), createDeck(Jack,_2)),
                createPlayer("Player1",createHand(Ace,Queen)),
                createPlayer("Player2",createHand(King, King, King)),
                createPlayer("Player3",createHand(_7,_3,Jack))).getWinners().toString());
    }
    
    @Test
    public void given_five_players_crupier_with_less_than_17_and_deck_should_determine_winners() {
        assertEquals("[Player2, Player3, Player4]", createGame(createCrupier(createHand(_6,_4), createDeck(Jack,_2)),
                createPlayer("Player1",createHand(King, King, King)),
                createPlayer("Player2",createHand(Queen,Ace)),
                createPlayer("Player3",createHand(_4,_10,_7)),
                createPlayer("Player4",createHand(Ace,Jack)),
                createPlayer("Player5",createHand(Ace,_5,_4))).getWinners().toString());
    }
    

    public Hand createHand(Card... cardsArray) {
        return new Hand() {         
            Card[] cards = cardsArray.clone();
            
            @Override
            public int value() {
                return canUseAceExtendedValue() ? sum() + 10 : sum();
            }

            @Override
            public boolean isBlackJack() {
                return value() == 21 && cards.length == 2;
            }

            @Override
            public boolean isBust() {                
                return value() > 21;
            }

            private boolean containsAce() {
                return Stream.of(cards).anyMatch(c->c==Ace);
            }
            
            private boolean canUseAceExtendedValue() {
                return containsAce() && sum() <= 11;
            }

            private int sum() {
                return Stream.of(cards).mapToInt(c->c.value()).sum();
            }

            @Override
            public void addCard(Card card) {
                Card[] c = new Card[cards.length+1];
                System.arraycopy(cards, 0, c, 0, cards.length);
                c[cards.length] = card;
                cards = c;
            }
        };
    }

    public interface Hand {
        int value();
        boolean isBlackJack();
        boolean isBust();
        void addCard(Card card);
    }
   

    public enum Card {
        Ace, _2, _3, _4, _5, _6, _7, _8, _9, _10, Jack, Queen, King;

       
        public boolean isFace() {
            return this == Jack || this == Queen || this == King;            
        }

        public int value() {
            return isFace() ? 10 : ordinal() + 1;
        }

    }    
    
    public Player createPlayer(String name, Hand hand) {
        return new Player() {
            @Override
            public String toString() {
                return name;
            }

            @Override
            public Hand hand() {
                return hand;
            }
        };
    }
    
    public interface Player {
        @Override
        String toString();
        Hand hand();
    }
    
    public Crupier createCrupier(Hand hand, Deck deck) {
        return new Crupier() {          
            @Override
            public Hand hand() {
                validate();
                return hand;
            }
            
            private void validate() {
                while (hand.value() < 17) hand.addCard(deck.takeCard());
            }
        };
    }
    
    public interface Crupier {
        Hand hand();
    }
    
    public Deck createDeck(Card... cards) {
        return new Deck() {
            int actualCard = 0;
            
            @Override
            public Card takeCard() {
                return cards[actualCard++];
            }
        };
    }
    
    public interface Deck {
        Card takeCard();
    }
    
    public Game createGame(Crupier crupier, Player... players) {
        return new Game() {
            @Override
            public List<Player> getWinners() {
                List<Player> winners = new ArrayList();
                for (Player player : players) 
                    if(playerWinsCrupier(crupier, player)) winners.add(player);
                return winners;
            }
        };
    }
    
    public interface Game {
        List<Player> getWinners();
        static boolean playerWinsCrupier(Crupier c, Player p) {
                return (p.hand().isBlackJack() && !c.hand().isBlackJack())
                        || (p.hand().value() > c.hand().value() || c.hand().isBust()) && !p.hand().isBust();
        }
    }
}
