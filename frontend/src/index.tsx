import React from "react";
import ReactDOM from "react-dom/client";
import "./styles/index.css";
import App from "./components/App";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import AlgorithmPage from "./pages/AlgorithmPage/AlgorithmPage";

// Tim removed some boilerplate to keep things simple.
// We're using an older version of React here.
const router = createBrowserRouter([
  {
    path: "/",
    element: <App></App>,
  },
  {
    path: "Algorithm",
    element: <AlgorithmPage></AlgorithmPage>,
  },
]);

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
