package com.example.ifitcoach;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;


public class FormulaCalculations
{

    /* calculating user daily calorie intake */
    public String TDEE(String age, String weight, String height, String gender, String activitylevel)
    {
        double age2 = Double.parseDouble(age);
        double height2 = Double.parseDouble(height);
        double weight2 = Double.parseDouble(weight);

        double bmr;
        double tdee;

        String Result="";

        /* TDEE for female */
        if(gender.equals("Female"))
        {
            bmr =  655 + (9.6 * weight2) + (1.8 * height2) - (4.7 * age2);
            if(activitylevel.equals("Sedentary"))
            {
                tdee = bmr * 1.2;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", ""); /* remove commas */
            }
            else if(activitylevel.equals("Lightly Active"))
            {
                tdee = bmr * 1.375;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
            else if(activitylevel.equals("Moderately Active"))
            {
                tdee = bmr * 1.55;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
            else if(activitylevel.equals("Very Active"))
            {
                tdee = bmr * 1.725;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
        }
        /* tdee for male */
        else
        {
            bmr = 66 + (13.7 * weight2) + (5 * height2) - (6.8 * age2);
            if(activitylevel.equals("Sedentary"))
            {
                tdee = bmr * 1.2;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
            else if(activitylevel.equals("Lightly Active"))
            {
                tdee = bmr * 1.375;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
            else if(activitylevel.equals("Moderately Active"))
            {
                tdee = bmr * 1.55;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
            else if(activitylevel.equals("Very Active"))
            {
                tdee = bmr * 1.725;
                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                Result = nf.format(tdee);
                Result = Result.replace(",", "");
            }
        }
        return Result;
    }



    /* calculate reamining calories progress bar value */
    public int ProgressBarValue(String tdee, String caloriesremaining)
    {
        String result;

        double tdee2 = Double.parseDouble(tdee);
        double caloriesremaining2 = Double.parseDouble(caloriesremaining);

        double progressbarvalue;

        progressbarvalue = ((tdee2 - caloriesremaining2) * 100) / tdee2;

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        result = nf.format(progressbarvalue);
        result = result.replace(",", "");

        return Integer.parseInt(result);
    }



    /* calculate remaining calories */
    public String CaloriesRemaining(String tdee, String food, String workout)
    {
        String result;

        double tdee2 = Double.parseDouble(tdee);
        double food2 = Double.parseDouble(food);
        double workout2 = Double.parseDouble(workout);

        double remaining;

        remaining = (tdee2 + workout2) - food2;

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        result = nf.format(remaining);
        result = result.replace(",", "");

        return result;
    }



    /* calculate BMI */
    public String BMI(String weight, String height)
    {
        String result;

        double height2 = Double.parseDouble(height);
        double weight2 = Double.parseDouble(weight);

        double bmi;

        bmi = weight2 / ((height2/100)*(height2/100));

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        result = nf.format(bmi);
        result = result.replace(",", "");

        return result;
    }


    /* determine health status*/
    public String HealthStatus(String bmi)
    {

        double bmi2 = Double.parseDouble(bmi);

        String Status;

        if( bmi2 < 18.5 )
        {
            Status = "Underweight";
        }
        else if ((bmi2 >= 18.5) && (bmi2 < 24.9))
        {
            Status = "HealthyWeight";
        }
        else if ((bmi2 >= 24.9) && (bmi2 < 29.9))
        {
            Status = "Overweight";
        }
        else
        {
            Status = "Obesity";
        }

        return Status;
    }



    /* ftin to cm converter*/
    public String ftintocm(String ft, String in)
    {
        String result;

        double ft2 = Double.parseDouble(ft);
        double in2 = Double.parseDouble(in);

        double height = (ft2 * 30.48) + (in2 * 2.54);

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        result = nf.format(height);
        result = result.replace(",", "");

        return result;
    }



    /* lbs to kg converter*/
    public String lbstokg(String lbs)
    {
        String result;

        double lbs2 = Double.parseDouble(lbs);

        double weight = (lbs2 *  0.45359237);

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        result = nf.format(weight);
        result = result.replace(",", "");

        return result;
    }



    /* age calculation */
    public String Age(String year)
    {
        String result;

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int userbyear = Integer.parseInt(year);
        int age = currentYear - userbyear;

        result = String.valueOf(age);

        return result;
    }


    /* lost weight calculation */
    public String LostWeight(String startWeight, String currentWeight)
    {
        String result;

        double userStartWeight = Double.parseDouble(startWeight);
        double userCurrentWeight = Double.parseDouble(currentWeight);

        double lostWeight = userStartWeight - userCurrentWeight;

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        result = nf.format(lostWeight);
        result = result.replace(",", "");

        return result;
    }



    /* remaining weight calculation */
    public String RemainingWeight(String currentWeight, String goalWeight)
    {
        String result;

        double userCurrentWeight = Double.parseDouble(currentWeight);
        double userGoalWeight = Double.parseDouble(goalWeight);

        double remainingWeight = userCurrentWeight - userGoalWeight;

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        result = nf.format(remainingWeight);
        result = result.replace(",", "");

        return result;
    }


}
