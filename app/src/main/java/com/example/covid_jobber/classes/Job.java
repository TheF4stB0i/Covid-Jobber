package com.example.covid_jobber.classes;

import com.example.covid_jobber.enums.ContractTime;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Job {
    int id;
    String title;
    String company;
    String description;
    String url;
    String city;
    double longitude;
    double latitude;
    String created;
    ContractTime contractTime;
    String category;


    //always in euro and always just the minimum
    int salary;



    public Job(){
        id = -1;
        title = "Start Swiping!";
        company = "";
        description = "";
        url = "";
        city = "";
        created = new Date().toString();
        contractTime = ContractTime.UNKNOWN;
        category = "";
        salary = 0;
    }

    public Job(JSONObject jobObject) throws JSONException {
        // Check if given object has id and if not the given object is not a proper jobObject
        try {
            this.id = Integer.parseInt(jobObject.get("id").toString());
        }catch (JSONException j){
            System.err.println("Could not construct JobOffer object from given JSONObject. Remember to only give the jobObject itself not the entire results array!");
            j.printStackTrace();
            return;
        }

        // Every jobObject should have these
        this.title = jobObject.getString("title").replace("<strong>","");
        this.title = this.title.replace("</strong>","");
        if(jobObject.getJSONObject("company").has("display_name")) {
            this.company = jobObject.getJSONObject("company").getString("display_name");
        }

        this.created = jobObject.getString("created");
        this.category = jobObject.getJSONObject("category").getString("label");
        if(jobObject.has("contract_time")){
            switch (jobObject.getString("contract_time")){
                case "full_time":
                    contractTime = ContractTime.FULL_TIME;
                    break;
                case "part_time":
                    contractTime = ContractTime.PART_TIME;
                    break;
            }
        }
        else {
            contractTime = ContractTime.EITHER;
        }



        // Better check if these are in the object
        this.url = jobObject.has("redirect_url") ? jobObject.getString("redirect_url") : "";
        this.salary = jobObject.has("salary_min") ? Integer.parseInt(jobObject.get("salary_min").toString()) : -1;
        this.description = jobObject.has("description") ? jobObject.get("description").toString() : "Keine Beschreibung vorhanden";

        this.description = this.description.replace("<strong>","");
        this.description = this.description.replace("</strong>","");

        if(jobObject.has("longitude") && jobObject.has("latitude")){
           longitude = Double.parseDouble(jobObject.get("longitude").toString());
           latitude = Double.parseDouble(jobObject.get("latitude").toString());
        }
        if(jobObject.has("location")){
            JSONArray area = jobObject.getJSONObject("location").getJSONArray("area");
            city = area.get(area.length()-1).toString();
        }

    }

    @NotNull
    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", city=" + city +
                ", created='" + created + '\'' +
                ", contractTime=" + contractTime +
                ", category='" + category + '\'' +
                ", salary=" + salary +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getcity() {
        return city;
    }

    public void setAddress(String city) {
        this.city = city;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public ContractTime getContractTime() {
        return contractTime;
    }

    public void setContractTime(ContractTime contractTime) {
        this.contractTime = contractTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }
}
