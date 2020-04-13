package com.defaup.newsgateway;

class Source
{
    String id, name, category;
    Source(String id, String name, String category)
    {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public String getCategory() {return category;}
    public String getId() {return id;}
    public String getName() {return name;}
}
