## **BetBot**
Bot consists of two modules: 
1. First module finds football games on 1xstavka.ru, that appropriate for special rule, then automaticaly do bets.
1. Second module collects matches results and displays statistics. 

#### **Used tecnologies:**
- Selenium
- PostgreSQL
- Stream API
- Multithreading
- Java 8 Data/Time API

------------

###### Output example:
    Parsing started.
    Finding leagues: complete
    Processing leagues: 245/245 (100.0%)
    Parsing completed in 00 min. 37 sec.
    --------------------------------------------------
    Total games - 715 (TODAY) 372 (TOMORROW) 
       RULE_ONE -   3 (TODAY)   4 (TOMORROW) 
      RULE_TEST -  10 (TODAY)  11 (TOMORROW) 
    --------------------------------------------------
    No appropriate betting games.
    --------------------------------------------------
    Bot work completed in 00 min. 39 sec.
###### 
	Analyse statistics for period from 2019-12-02 to 2019-12-07
    --------------------------------------------------
    No games to scan
    --------------------------------------------------
    Games for RULE_ONE:
      Total -    6 | 6   
         1W -    0 | 0   
          X -    1 | 1   
         W2 -    2 | 2   
        N/R -    3 | 3   
    --------------------------------------------------
    Starting chrome driver: complete
    --------------------------------------------------
    Process scanning: complete
    --------------------------------------------------
    Games for RULE_TEST:
      Total -   33 | 19  
         1W -    8 | 3   
          X -    8 | 4   
         W2 -    6 | 5   
        N/R -   11 | 7   
    --------------------------------------------------