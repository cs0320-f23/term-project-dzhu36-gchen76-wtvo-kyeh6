// This file contains our imaginary CSV files as well as creating and adding said CVS files
// to a hashmap which we can access in the REPL input.

let hashMap2DArrays = new Map<String, string[][]>();
let hashmapSearchResults = new Map<String, Map<String, string[][]>>();

export function hashmapData() {
  const jobs = [
    ["Name", "Age", "Job"],
    ["Ron", "12", "Engineer"],
    ["Avocado", "20", "Doctor"],
    ["Robert", "20", "Chef"],
    ["Andy Van Dam", "76", "Professor"],
  ];

  const income = [
    ["Income", "Year"],
    ["100000", "2012"],
    ["20000", "1998"],
    ["12000", "1572"],
    ["1", "2003"],
    ["6", "1808"],
    ["2023", "2023"],
    ["2", "2023"],
  ];

  const empty = [[], [], [], []];
  const noHeaders = [
    ["John", "12"],
    ["Ruby", "32"],
    ["Albert", "12"],
    ["Hoob", "156"],
    ["krobus", "32"],
  ];

  hashMap2DArrays = new Map<String, string[][]>();

  hashMap2DArrays.set("filepath/jobs", jobs);
  hashMap2DArrays.set("filepath/income", income);
  hashMap2DArrays.set("filepath/empty", empty);
  hashMap2DArrays.set("filepath/noHeaders", noHeaders);

  hashmapSearchResults = new Map<String, Map<String, string[][]>>();

  let jobsMap = new Map<String, string[][]>();
  // searching value with 1 result
  jobsMap.set("Name Avocado", [["Avocado", "20", "Doctor"]]);
  jobsMap.set("0 Avocado", [["Avocado", "20", "Doctor"]]);
  // searching value with repeated result
  jobsMap.set("Age 20", [
    ["Avocado", "20", "Doctor"],
    ["Robert", "20", "Chef"],
  ]);
  jobsMap.set("1 20", [
    ["Avocado", "20", "Doctor"],
    ["Robert", "20", "Chef"],
  ]);
  hashmapSearchResults.set("filepath/jobs", jobsMap);

  let incomeMap = new Map<string, string[][]>();
  // searching value that is not in csv
  incomeMap.set("Income 5", [[]]);
  incomeMap.set("0 5", [[]]);
  // searching for value that is in column, but also repeated in another column
  incomeMap.set("Income 2023", [["2023", "2023"]]);
  incomeMap.set("0 2023", [["2023", "2023"]]);
  // searching for value that is in a different column
  incomeMap.set("Year 1", [[]]);
  incomeMap.set("1 1", [[]]);

  hashmapSearchResults.set("filepath/income", incomeMap);

  // searching a completely empty map
  let emptyMap = new Map<String, string[][]>();
  emptyMap.set("Name Liam", [[]]);
  emptyMap.set("0 Liam", [[]]);
  hashmapSearchResults.set("filepath/empty", emptyMap);

  let noHeadersMap = new Map<String, string[][]>();
  // searching with no header with one result
  noHeadersMap.set("John", [["John", "12"]]);
  // searching with no header with multiple results
  noHeadersMap.set("12", [
    ["John", "12"],
    ["Albert", "12"],
  ]);
  hashmapSearchResults.set("filepath/noHeaders", noHeadersMap);
}
export function returnArrayMap() {
  return hashMap2DArrays;
}
export function returnSearchMap() {
  return hashmapSearchResults;
}
