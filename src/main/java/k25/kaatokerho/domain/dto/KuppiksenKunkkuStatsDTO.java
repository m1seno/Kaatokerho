package k25.kaatokerho.domain.dto;

public record KuppiksenKunkkuStatsDTO(
        String season,
        int gpCount,
        Long currentChampionId,
        String currentChampionName,
        int uniqueChampions,
        int totalChallenges
) {}