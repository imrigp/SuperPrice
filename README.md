# Prices

Parse and store Israeli supermarket chain stores' prices.  
The law obligates all large supermarket chains to publish the price (including promos) of every item in each of their stores.  
Although there is a uniform format they should follow, they don't fully comply it it so measures need to be taken in order to collect the data uniformly.  
Furthermore, each chain publishes the data on their own website, which requires special parsing for every chain.

# Goal

Build a robust server which crawls all chain websites periodically to find new updates and update all data to database.  
Maintain a uniform database while handling conflicting data introduced by different chains.  
Due to the large number of chain stores, concurrency is used to render the crawling efficient.  

# Future

Build REST API to query the database in meaningful ways:
- Search for specific items in specific stores (geographically filtered)
- Build a desired shopping cart and check which store provides the best overall price, including discounts
- Track price history
