package CSTEpisodicMemory.episodic;


import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class EpisodicBindingTest {

    public EpisodicBindingTest(){

    }

    @Test
    public void temporalRelationsTest(){
        assertEquals("Meet",
                EpisodeBinding.temporalRelation(0, 1000,
                                             1000, 2000));

        assertEquals("Before",
                EpisodeBinding.temporalRelation(0, 1000,
                                             1500, 2000));

        assertEquals("During",
                EpisodeBinding.temporalRelation(500, 1000,
                        0, 2000));

        assertEquals("Overlap",
                EpisodeBinding.temporalRelation(0, 1000,
                        700, 2000));

        assertEquals("Start",
                EpisodeBinding.temporalRelation(0, 1000,
                        0, 2000));

        assertEquals("Finish",
                EpisodeBinding.temporalRelation(500, 1000,
                        0, 1000));

        assertEquals("Equal",
                EpisodeBinding.temporalRelation(0, 1000,
                        10, 1010));
    }
}
