import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
});

test("checking mode starts in brief and can switch between brief and verbose", async ({
  page,
}) => {
  // make sure the history box exists
  await expect(page.getByTitle("repl-history")).toBeVisible;
  await page.getByLabel("Command input").click();
  //input just hello to get unknown input response
  await page.getByLabel("Command input").fill("hello");
  await page.getByRole("button", { name: "Submitted 0 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Unknown input, please try again.");
  //input a long string of nonsense to get the too long response
  await page
    .getByLabel("Command input")
    .fill("hello aljshdljashd aljshdlaksjhd asd asd");
  await page.getByRole("button", { name: "Submitted 1 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Command too long!");
  // switch mode to verbose
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 2 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Command: mode, Output: Mode switched to verbose");
  // switch mode back to brief
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 3 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Mode switched to brief");
});

test("test load", async ({ page }) => {
  // testing for file that is not in the dataset
  await page.getByLabel("Command input").fill("load_file lajhdasjh");
  await page.getByRole("button", { name: "Submitted 0 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: File not found!");
  // testing for file that is in the dataset
  await page.getByLabel("Command input").fill("load_file filepath/jobs");
  await page.getByRole("button", { name: "Submitted 1 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: File: 'filepath/jobs' loaded");
});

test("test view", async ({ page }) => {
  // testing if view is called before load is
  // brief
  await page.getByLabel("Command input").fill("view");
  await page.getByRole("button", { name: "Submitted 0 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Please load a file first!");
  // verbose
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 1 times" }).click();
  await page.getByLabel("Command input").fill("view");
  await page.getByRole("button", { name: "Submitted 2 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Command: view, Output: Please load a file first!");
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 3 times" }).click();
  // testing view table by seeing if an element is displayed on screen
  await page.getByLabel("Command input").fill("load_file filepath/jobs");
  await page.getByRole("button", { name: "Submitted 4 times" }).click();
  await page.getByLabel("Command input").fill("view");
  await page.getByRole("button", { name: "Submitted 5 times" }).click();
  await expect(
    page
      .getByTitle("viewing-table")
      .getByTitle("view-table")
      .getByTitle("row")
      .getByTitle("cell")
      .first()
  ).toHaveText("Name");
  // checking if I load a new file and view it the new information is updated
  await page.getByLabel("Command input").fill("load_file filepath/income");
  await page.getByRole("button", { name: "Submitted 6 times" }).click();
  await page.getByLabel("Command input").fill("view");
  await page.getByRole("button", { name: "Submitted 7 times" }).click();
  await expect(
    page
      .getByTitle("viewing-table")
      .getByTitle("view-table")
      .getByTitle("row")
      .getByTitle("cell")
      .first()
  ).toHaveText("Income");
});

test("test search", async ({ page }) => {
  // testing if search is called before load is
  // brief
  await page.getByLabel("Command input").fill("search asdas dasds");
  await page.getByRole("button", { name: "Submitted 0 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Please load a file first!");
  // verbose
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 1 times" }).click();
  await page.getByLabel("Command input").fill("search asdas dasds");
  await page.getByRole("button", { name: "Submitted 2 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText(
    "Command: search asdas dasds, Output: Please load a file first!"
  );
  await page.getByLabel("Command input").fill("mode");
  await page.getByRole("button", { name: "Submitted 3 times" }).click();
  // testing search table by seeing if an element is displayed on screen
  await page.getByLabel("Command input").fill("load_file filepath/jobs");
  await page.getByRole("button", { name: "Submitted 4 times" }).click();
  // with header
  await page.getByLabel("Command input").fill("search Name Avocado");
  await page.getByRole("button", { name: "Submitted 5 times" }).click();
  await expect(
    page
      .getByTitle("viewing-table")
      .getByTitle("view-table")
      .getByTitle("row")
      .getByTitle("cell")
      .first()
  ).toHaveText("Avocado");
  // without headers
  await page.getByLabel("Command input").fill("search 0 Avocado");
  await page.getByRole("button", { name: "Submitted 6 times" }).click();
  await expect(
    page
      .getByTitle("viewing-table")
      .getByTitle("view-table")
      .getByTitle("row")
      .getByTitle("cell")
      .first()
  ).toHaveText("Avocado");
});

test("test misc.", async ({ page }) => {
  // testing if user clicks button with nothing in input box
  await page.getByRole("button", { name: "Submitted 0 times" }).click();
  await expect(
    page.getByTitle("repl-history").getByTitle("history-line").last()
  ).toHaveText("Output: Unknown input, please try again.");
  // testing search empty table by seeing if no element is displayed on screen
  await page.getByLabel("Command input").fill("load_file filepath/empty");
  await page.getByRole("button", { name: "Submitted 1 times" }).click();
  // with header
  await page.getByLabel("Command input").fill("search Name Liam");
  await page.getByRole("button", { name: "Submitted 2 times" }).click();
  await expect(
    page.getByTitle("viewing-table").getByTitle("view-table").getByTitle("row")
  ).toHaveText("");
  // without headers
  await page.getByLabel("Command input").fill("search 0 Liam");
  await page.getByRole("button", { name: "Submitted 3 times" }).click();
  await expect(
    page.getByTitle("viewing-table").getByTitle("view-table").getByTitle("row")
  ).toHaveText("");
});
