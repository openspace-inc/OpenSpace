package com.example.iaso.PersonalPage;

public class DynamicHabit {
    String name;
    int blocks;
    int streak;
    String type;
    String description;
    int amount;
    int time;
    String imageName;
    int timeInvested;
    String stockSymbol;

    public DynamicHabit(String name1, int streak1, String type1, String description1, int amount1, int time1, String imageName1, int timeInvested1){
        name = name1;
        streak = streak1;
        type = type1;
        description = description1;
        blocks = 1; //initial investment
        amount = amount1;
        time = time1;
        imageName = imageName1;
        timeInvested = timeInvested1;
        stockSymbol = generateStockSymbol(name1);
    }

    public DynamicHabit(String name1, int streak1, String type1, String description1, int time1, String imageName1, int timeInvested1, int blocks1){
        name = name1;
        streak = streak1;
        type = type1;
        description = description1;
        blocks = blocks1; //initial investment
        amount = 1;
        time = time1;
        imageName = imageName1;
        timeInvested = timeInvested1;
        stockSymbol = generateStockSymbol(name1);
    }

    public String getName3(){
        return name;
    }

    public String getType(){
        return type;
    }

    public int getStreak3(){
        return streak;
    }

    public int getBlocks3(){
        return blocks;
    }
    public String getDescription(){
        return description;
    }
    public int getAmount3(){return amount;}

    public int getTime(){
        return time;
    }
    public String getImageName(){
        return imageName;
    }
    public int getTimeInvested(){return timeInvested;}

    public String getStockSymbol(){
        return stockSymbol;
    }

    private String generateStockSymbol(String habitName){
        if(habitName == null || habitName.isEmpty()){
            return "";
        }

        String cleaned = habitName.toUpperCase().replaceAll("[^A-Z]", "");

        if(cleaned.length() <= 4){
            return cleaned;
        }

        String vowels = "AEIOU";
        StringBuilder builder = new StringBuilder();
        builder.append(cleaned.charAt(0));

        // Append consonants in order after the first character
        for(int i = 1; i < cleaned.length() && builder.length() < 4; i++){
            char c = cleaned.charAt(i);
            if(vowels.indexOf(c) == -1){
                builder.append(c);
            }
        }

        // Append remaining characters if necessary
        for(int i = 1; i < cleaned.length() && builder.length() < 4; i++){
            char c = cleaned.charAt(i);
            if(vowels.indexOf(c) != -1){
                builder.append(c);
            }
        }

        while(builder.length() < 4){
            builder.append(cleaned.charAt(cleaned.length()-1));
        }

        if(builder.length() > 4){
            builder.setLength(4);
        }

        return builder.toString();
    }
}
