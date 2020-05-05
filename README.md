## **BetBot**
Bot consists of two modules: 
1. First module finds football games on 1xstavka.ru, that appropriate for special ruleFilter, then automaticaly do bets.
1. Second module collects matches results and displays statistics. 

#### **Used tecnologies:**
- Spring framework
- Spring boot
- Spring data jpa
- Hibernate
- PostgreSQL
- REST API
- Selenium
- Multithreading
- Java 8 Data/Time API, Stream API

------------

###### Output example:
	BetBot started at 20:59 15.04.2020
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Finding leagues: complete.
    Parsing leagues: 30/30 (100.0%)
    --------------------------------------------------
    X_TWO:   1(+0)   0(+0)
    FW_SW:  15(+0)   3(+3)
    --------------------------------------------------
    No appropriate betting games.
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Scanning game results started.
    --------------------------------------------------
    Starting web driver: complete.
    --------------------------------------------------
    Scanning games: 1/3 (33.3%)
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Analyse statistics for period from null to null
    --------------------------------------------------
    X_TWO:  53| 50| 62   8,75|-26,13|-20,88  -3,99|-19,40
    FW_SW:  33| 42| 26  -4,27|  5,10|-24,96   1,89| -7,58
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Bot work completed in 00 min. 59 sec.