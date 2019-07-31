package org.aion.unity.distribution;

import avm.Address;
import org.aion.unity.distribution.schemes.F1ToyRewardsManager;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.aion.unity.distribution.model.RewardsManager.Event;
import static org.aion.unity.distribution.model.RewardsManager.EventType;
import static org.junit.Assert.assertEquals;

public class F1ToyRewardsManagerTest {

    private Address addressOf(int n) {
        byte[] b = new byte[32];

        byte[] trimmed = BigInteger.valueOf(n).toByteArray();
        int padding = 32 - trimmed.length;

        System.arraycopy(trimmed, 0, b, padding, trimmed.length);

        return new Address(b);
    }

    @Test
    public void testSingleUser1() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(0L, rewards.get(addressOf(1)).longValue());
    }

    @Test
    public void testSingleUser2() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.UNVOTE, addressOf(1), 3002, 2d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(0L, rewards.get(addressOf(1)).longValue());
    }

    @Test
    public void testSingleUser3() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.BLOCK, addressOf(100), 3002, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(5000L, rewards.get(addressOf(1)).longValue());
    }

    @Test
    public void testSingleUser4() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(5000L, rewards.get(addressOf(1)).longValue());
    }

    @Test
    public void testTwoUsers1() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.VOTE, addressOf(2), 3001, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(2000L, rewards.get(addressOf(1)).longValue());
        assertEquals(3000L, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testTwoUsers2() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(2000L, rewards.get(addressOf(1)).longValue());
        assertEquals(3000L, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testTwoUsers3() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d),
                new Event(EventType.BLOCK, addressOf(100), 3008, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(4000L, rewards.get(addressOf(1)).longValue());
        assertEquals(6000L, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testTwoUsers4() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d),
                new Event(EventType.UNVOTE, addressOf(2), 3006, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3008, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(7000L, rewards.get(addressOf(1)).longValue());
        assertEquals(3000L, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testWithdraw() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3005, 5000d),
                new Event(EventType.UNVOTE, addressOf(2), 3006, 3d),
                new Event(EventType.WITHDRAW, addressOf(2), 3007, null),
                new Event(EventType.BLOCK, addressOf(100), 3008, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(7000L, rewards.get(addressOf(1)).longValue());
        assertEquals(3000L, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testInconstantBlockRewards() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.BLOCK, null, 3002, 4000d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, null, 3004, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(4000 + 5000 * 2/5, rewards.get(addressOf(1)).longValue());
        assertEquals( 5000 * 3/5, rewards.get(addressOf(2)).longValue());
    }

    @Test
    public void testDoubleVote() {
        List<Event> events = Arrays.asList(
                new Event(EventType.VOTE, addressOf(1), 3001, 2d),
                new Event(EventType.BLOCK, addressOf(100), 3002, 5000d),
                new Event(EventType.VOTE, addressOf(1), 3003, 2d),
                new Event(EventType.VOTE, addressOf(2), 3003, 3d),
                new Event(EventType.BLOCK, addressOf(100), 3004, 5000d)
        );
        Map<Address, Double> rewards = new F1ToyRewardsManager().computeRewards(events);
        assertEquals(5000 + 5000 * 4/7, rewards.get(addressOf(1)).longValue());
        assertEquals(5000 * 3/7, rewards.get(addressOf(2)).longValue());
    }
}
