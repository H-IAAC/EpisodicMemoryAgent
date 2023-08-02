package CSTEpisodicMemory.episodic;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EpisodicBindingTest {

    public EpisodicBindingTest(){

    }

    @Test
    public void temporalRelationsTest(){
        Assertions.assertEquals("Meet", EpisodeBinding.temporalRelation(0, 1000,
                                     1000, 2000));

        Assertions.assertEquals("Before", EpisodeBinding.temporalRelation(0, 1000,
                                     1500, 2000));

        Assertions.assertEquals("During", EpisodeBinding.temporalRelation(500, 1000,
                0, 2000));

        Assertions.assertEquals("Overlap", EpisodeBinding.temporalRelation(0, 1000,
                700, 2000));

        Assertions.assertEquals("Start", EpisodeBinding.temporalRelation(0, 1000,
                0, 2000));

        Assertions.assertEquals("Finish", EpisodeBinding.temporalRelation(500, 1000,
                0, 1000));

        Assertions.assertEquals("Equal", EpisodeBinding.temporalRelation(0, 1000,
                10, 1010));
    }
}
