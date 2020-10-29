# SuperPrice

A Java RESTful web service which provides real-time data about supermarket chains, stores, items, prices, and (soon) discounts.   
There are 800+ stores, more than 120k items, and 4mil+ price records, which update regularly.
## Production

I set up a production server:  
http://api.imri.ga  
I'm working on a website which utilizes the API to demonstrate the possibilities, but for now you could explore and test the API in the OpenApi page [here](http://api.imri.ga/ui).
<br><br>
![image](https://user-images.githubusercontent.com/57985724/97604988-c7493b00-1a16-11eb-8474-ecc54d21c6b9.png)

## Background

The law ("שקיפות מחירים") obligates all large supermarket chains in Israel to publish the price (including discounts) of every item in each of their stores.  
Although there is a uniform format they should follow, they don't fully comply it, so measures need to be taken in order to collect the data uniformly.  
Furthermore, each chain publishes the data on their own website, which requires special parsing plan for every chain.


## Goal

Develop a robust server which crawls all chains' websites periodically to find new updates and store them in a database.  
Maintain a uniform database while handling conflicting data introduced by different chains.  
Then, expose a user-friendly API to query meaningful data. 

## Ideas

The API could be used for interesting purposes:
- Search for specific items in specific stores (geographically filtered)
- Build a shopping cart and check which store provides the best overall price, including discounts
- Track price history

## Future

- I intend to replace the somewhat naive item search function with Apache Solr. 
Although the search itself is quite efficient right now (using compressed suffix tree), it lacks ranked searching, highlighting and fuzzy matching.  
- I'm currently working on a website which will show the API capabilities.  
- Finish indexing all chains (few left).
- Add discounts to the database as well.
