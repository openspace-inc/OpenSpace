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
        if(habitName == null || habitName.trim().isEmpty()){
            return "";
        }

        // Remove any characters that are not letters or spaces
        String cleanedName = habitName.replaceAll("[^A-Za-z ]", " ").trim();
        if(cleanedName.isEmpty()){
            return "";
        }

        // Uppercase form used throughout
        String upper = cleanedName.toUpperCase();

        // Handle a few well known names directly
        String upperNoSpace = upper.replaceAll(" ", "");
        switch(upperNoSpace){
            case "APPLEINC":
            case "APPLE":
                return "AAPL";
            case "TESLA":
                return "TSLA";
            case "MICROSOFT":
            case "MICROSOFTCORPORATION":
                return "MSFT";
            default:
                break;
        }

        // Split into words and start with their initials if there is more than one
        String[] words = upper.split("\\s+");
        String candidate;
        if(words.length > 1){
            StringBuilder b = new StringBuilder();
            for(String w : words){
                if(!w.isEmpty()){
                    b.append(w.charAt(0));
                    if(b.length() == 4){
                        break;
                    }
                }
            }
            candidate = b.toString();
            // If we got only one letter, fall back to first word characters
            if(candidate.length() < 2){
                candidate = words[0];
            }
        }else{
            candidate = upperNoSpace;
        }

        // Remove any remaining non letters and ensure uppercase
        candidate = candidate.replaceAll("[^A-Z]", "");

        // If the candidate is still too long, drop vowels after the first letter
        if(candidate.length() > 4){
            String start = String.valueOf(candidate.charAt(0));
            String remainder = candidate.substring(1).replaceAll("[AEIOU]", "");
            candidate = (start + remainder);
        }

        if(candidate.length() > 4){
            candidate = candidate.substring(0,4);
        }

        if(candidate.length() < 2 && upperNoSpace.length() >= 2){
            candidate = upperNoSpace.substring(0, Math.min(4, upperNoSpace.length()));
        }

        return candidate;
    }
}
