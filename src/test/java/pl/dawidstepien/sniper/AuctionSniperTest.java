package pl.dawidstepien.sniper;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.dawidstepien.sniper.AuctionEventListener.PriceSource;

@RunWith(JMock.class)
public class AuctionSniperTest {

  private final Mockery context = new Mockery();

  private final SniperListener sniperListener = context.mock(SniperListener.class);

  private final Auction auction = context.mock(Auction.class);

  private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);

  private final States sniperState = context.states("sniper");

  @Test
  public void reportsLostIfAuctionClosesImmediately() {
    context.checking(new Expectations() {{
      atLeast(1).of(sniperListener).sniperLost();
    }});

    sniper.auctionClosed();
  }

  @Test
  public void reportsLostIfAuctionClosesWhenBidding() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperBidding();
        then(sniperState.is("bidding"));
      atLeast(1).of(sniperListener).sniperLost();
        when(sniperState.is("bidding"));
    }});

    sniper.currentPrice(123, 45, PriceSource.FROM_OTHER_BIDDER);
    sniper.auctionClosed();
  }

  @Test
  public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
    final int price = 1001;
    final int increment = 25;

    context.checking(new Expectations() {{
      one(auction).bid(price + increment);
      atLeast(1).of(sniperListener).sniperBidding();
    }});

    sniper.currentPrice(price, increment, PriceSource.FROM_OTHER_BIDDER);
  }

  @Test
  public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
    context.checking(new Expectations() {{
      atLeast(1).of(sniperListener).sniperWinning();
    }});

    sniper.currentPrice(123, 45, PriceSource.FROM_SNIPER);
  }

  @Test
  public void reportsWonIfAuctionClosesWhenWinning() {
    context.checking(new Expectations() {{
      ignoring(auction);
      allowing(sniperListener).sniperWinning();
        then(sniperState.is("winning"));
      atLeast(1).of(sniperListener).sniperWon();
        when(sniperState.is("winning"));
    }});

    sniper.currentPrice(123, 45, PriceSource.FROM_SNIPER);
    sniper.auctionClosed();
  }
}
