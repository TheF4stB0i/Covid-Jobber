package com.example.covid_jobber.classes;

import com.example.covid_jobber.enums.ContractTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Job {
    int id;
    String title;
    String company;
    String description;
    String url;
    Address address;
    String created;
    ContractTime contractTime;
    String category;

    //always in euro and always just the minimum
    double salary;

    public Job(){
        id = -1;
        title = "Straßenplaner";
        company = "Stadt Mannheim";
        description = "Wir suchen inkompetente Straßenplaner für unsere Stadt";
        url = "";
        address.setCity("Mannheim");
        address.setCountry("Germany");
        address.setDisplayName("E 5, 68159 Mannheim");
        created = new Date().toString();
        contractTime = ContractTime.FULL_TIME;
        category = "Öffentliche Arbeit";
        salary = 10000.0;
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
        this.title = jobObject.getString("title");
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
                default:
                    contractTime = ContractTime.EITHER;
                    break;
            }
        }


        // Better check if these are in the object
        this.url = jobObject.has("redirect_url") ? jobObject.getString("redirect_url") : "";
        this.salary = jobObject.has("salary_min") ? Double.parseDouble(jobObject.get("salary_min").toString()) : -1.0;


    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", address=" + address +
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}
