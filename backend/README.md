## Project Details
Sprint 2: Server\
Team members: 
- Michael (Hung-Jen) Wang _hwang262_
- Grace Chen _gchen76_

Hours: 20\
Repo: https://github.com/cs0320-f23/server-wanghungjen-GChen04.git

## Design Choices
In order for viewCsv and searchCsv to maintain their functionality, it is necessary for loadCsv to 
store the parsed CSV file after loading from the filepath. Thus, we decided to implement a dependency 
injection design, in which a CsvData class is inherited by all three CSV functionality classes. By 
using _getters_ and _setters_ appropriately with defensive mechanisms, we are able to pass the parsed 
CSV data between all three functionality classes safely.

As for the broadband handler, we decided to call once for all states and their codes to store 
appropriately, so further reuses would only require calls to the county codes. As for the caching 
criteria, we decided to cache each API call to the census’ broadband data. The design includes a 
wrapper class (CachedApiCall) that wraps ApiCall itself, allowing the caching functionality to be 
efficient whenever multiple, identical broadband requests are made to our server. As for the 
flexibility required, we allowed _maximum entries_ and _maximum storage minutes_ to be passed in as 
parameters in Server to adjust accordingly for developers. The strategy pattern would be our Query 
interface, in which it could take in any type as the target and result for each query. Moreover,
the ApiCall, CachedApiCall, and MockQuery classes all implement the interface -- the BroadBandHandler
takes in an instance of type Query into its constructor, allowing any of these three classes (or a new
one) to be used as developers see fit.

## Errors/Bugs
We assumed the _404 Not Found_ page as normal when the _localhost_ link is clicked initially without 
endpoints specified. The Sprint didn’t account for this, but we wanted to mention this scenario 
explicitly in case this is an edge case that should be considered.

## Tests
For tests on CSV parsing and searching, we mainly delved into the _Response code_ (i.e., messages like
"success" or "error_datasource") and _Response Map_. There are multiple error edge cases we considered, 
and each is commented and tested accordingly in the _csvhandlingtests_ package. Multiple unit tests 
are also conducted to ensure correct parsing of rows, and the search functionality is performing correctly.

For tests on broadband, we used a MockQuery with _0.0_ as the mock broadband result to prevent 
testing functionality from constantly querying to the ACS census. Similarly, there are multiple 
error edge cases we considered, and each is commented and tested accordingly in the _broadbandtests_ 
package. Within _TestBroadBandHandler_, we mock-queried to ensure local functionality performs 
successfully. Within _TestQuery_, we queried directly to the census with the ground truth already 
extracted manually to test successful calls. This combination ensures that our responses to the 
users are correct, and our requests to the census also yield correct results.

These tests can be run by clicking the green arrow button to the left of each test method's
header in each testing class.

## How to...
Running the Server requires clicking the Run ‘Server’ button on IntelliJ after entering the Server
class. A _404 Not found_ page means localhost has been successfully run, and the following endpoints 
can be leveraged.

- Passing in _/loadcsv?filepath=[**filepath**]_, allows a CSV file to be loaded to the server, 
  assuming that the file path given is correct, and the CSV path is indeed in the data package. 
  Please do not load CSV files in which numbers are chosen as the header, as this would confuse the 
  search functionality when choosing the header or index as the column identifier.
- Passing in _/viewcsv_ allows users to view the parsed CSV file, assuming loadcsv was successful.
- Passing in _/searchcsv?target=[**target**]_ allows the search functionality across the entire CSV 
  data with the target as target. This returns the appropriate rows accordingly given loadcsv was 
  loaded successfully.
- Passing in _/searchcsv?target=[**target**]&identifier=[**identifier**]_ is similar to the case 
  above, but a specific column identifier could be passed in as well. This should be an index if the 
  CSV file doesn’t contain a header row and a header if it does contain a header row.
- Passing in _/broadband?state=[**state**]&county=[**county**]_ returns the broadband data of the 
  given state and county within the United States of America and the retrieved date & time. Please 
  note that some counties do not have broadband data within the census, so it is important to check 
  the error responses accordingly.

Changing configurations of the cache can also be performed. Simply change the parameters of 
_maxEntires_ and _maxStorageMin_ in the Server class for the desired configuration.
