package com.example.ifitcoach;

public class Foods
{
    public String foodname, foodservingsizeunit, foodcalories, foodcarbs, foodfat, foodprotein, foodcreator;

    public Foods()
    {

    }

    public Foods(String foodname, String foodservingsizeunit, String foodcalories, String foodcarbs, String foodfat, String foodprotein, String foodcreator)
    {
        this.foodname = foodname;
        this.foodservingsizeunit = foodservingsizeunit;
        this.foodcalories = foodcalories;
        this.foodcarbs = foodcarbs;
        this.foodfat = foodfat;
        this.foodprotein = foodprotein;
        this.foodcreator = foodcreator;
    }

    public String getFoodname() {
        return foodname;
    }

    public void setFoodname(String foodname) {
        this.foodname = foodname;
    }


    public String getFoodservingsizeunit() {
        return foodservingsizeunit;
    }

    public void setFoodservingsizeunit(String foodservingsizeunit) {
        this.foodservingsizeunit = foodservingsizeunit;
    }

    public String getFoodcalories() {
        return foodcalories;
    }

    public void setFoodcalories(String foodcalories) {
        this.foodcalories = foodcalories;
    }

    public String getFoodcarbs() {
        return foodcarbs;
    }

    public void setFoodcarbs(String foodcarbs) {
        this.foodcarbs = foodcarbs;
    }

    public String getFoodfat() {
        return foodfat;
    }

    public void setFoodfat(String foodfat) {
        this.foodfat = foodfat;
    }

    public String getFoodprotein() {
        return foodprotein;
    }

    public void setFoodprotein(String foodprotein) {
        this.foodprotein = foodprotein;
    }

    public String getFoodcreator() {
        return foodcreator;
    }

    public void setFoodcreator(String foodcreator) {
        this.foodcreator = foodcreator;
    }
}
