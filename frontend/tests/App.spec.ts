// import { test, expect } from "@playwright/test";

// test.beforeEach(() => {
//   await page.goto("http://localhost:8000/");
// });

// test("on page load, i see an input bar", async ({ page }) => {
//   //await page.goto("http://localhost:8000/");
//   await expect(page.getByLabel("Command input")).toBeVisible();
// });

// test("after I type into the input box, its text changes", async ({ page }) => {
//   // Step 1: Navigate to a URL
//   //await page.goto("http://localhost:8000/");

//   // Step 2: Interact with the page
//   // Locate the element you are looking for
//   await page.getByLabel("Command input").click();
//   await page.getByLabel("Command input").fill("Awesome command");

//   // Step 3: Assert something about the page
//   // Assertions are done by using the expect() function
//   const mock_input = `Awesome command`;
//   await expect(page.getByLabel("Command input")).toHaveValue(mock_input);
// });

// test("on page load, i see a button", async ({ page }) => {
//   //await page.goto("http://localhost:8000/");
//   await expect(page.getByRole("button")).toBeVisible();
// });

// test("after I click the button, its label increments", async ({ page }) => {
//   // TODO WITH TA: Fill this in to test your button counter functionality!
//   //await page.goto("http://localhost:8000/");
//   await expect(
//     page.getByRole("button", { name: "Submitted 0 times" })
//   ).toBeVisible();
//   await page.getByRole("button", { name: "Submitted 0 times" }).click();
//   await page.getByRole("button", { name: "Submitted 1 times" }).click();
//   await page.getByRole("button", { name: "Submitted 2 times" }).click();
//   await page.getByRole("button", { name: "Submitted 3 times" }).click();
//   await expect(
//     page.getByRole("button", { name: "Submitted 4 times" })
//   ).toBeVisible();
// });

// test("after I click the button, my command gets pushed", async ({ page }) => {
//   // TODO: Fill this in to test your button push functionality!
// });