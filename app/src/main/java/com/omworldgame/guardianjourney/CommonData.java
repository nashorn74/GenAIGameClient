package com.omworldgame.guardianjourney;

public class CommonData {

    // 종족 배열
    public static final String[] RACES = {
            "엘프", "인간", "드워프", "하프-오크", "페어리", "서큐버스", "하프-엘프", "다크엘프"
    };

    // 직업 배열
    public static final String[] JOBS = {
            "궁수", "마법사", "전사", "치료사", "암살자", "정령사", "숲의 수호자", "성기사", "매혹술사", "공주", "용병단 보좌관", "왕국의 기사"
    };

    // 직업별 스탯 배열
    public static final int[][] JOB_STATS = {
            {1000, 100, 50, 10, 5},   // 궁수
            {500, 50, 200, 20, 3},    // 마법사
            {1500, 150, 30, 25, 10},  // 전사
            {600, 80, 100, 5, 2},     // 치료사
            {700, 120, 40, 30, 4},    // 암살자
            {600, 60, 150, 15, 5},    // 정령사
            {1200, 100, 70, 20, 8},   // 숲의 수호자
            {1400, 120, 60, 18, 12},  // 성기사
            {400, 40, 150, 12, 4},    // 매혹술사
            {1000, 90, 80, 10, 5},    // 공주
            {1100, 130, 60, 17, 9},   // 용병단 보좌관
            {1300, 140, 50, 22, 11}   // 왕국의 기사
    };

    // 특정 직업의 스탯을 가져오는 메서드
    public static int[] getStatsForJob(String job) {
        int index = java.util.Arrays.asList(JOBS).indexOf(job);
        if (index != -1) {
            return JOB_STATS[index];
        }
        return null;  // 직업이 존재하지 않을 경우
    }

    // hp, mp, attack_point, defence_point 값을 기준으로 직업을 추정하는 함수
    public static String getJobByStats(int hp, int mp, int attackPoint, int defencePoint) {
        for (int i = 0; i < JOBS.length; i++) {
            int[] stats = JOB_STATS[i];
            if (stats[1] == hp && stats[2] == mp && stats[3] == attackPoint && stats[4] == defencePoint) {
                return JOBS[i];
            }
        }
        return "알 수 없는 직업";  // 스탯과 일치하는 직업이 없을 때
    }
}
