import "../styles/main.css";
import { Dispatch, SetStateAction, useState, useEffect } from "react";
import { ControlledInput } from "./ControlledInput";
import { returnArrayMap } from "../Data/mockHashmap";
import { returnSearchMap } from "../Data/mockHashmap";
import { hashmapData } from "../Data/mockHashmap";

interface REPLInputProps {
  history: string[];
  setHistory: Dispatch<SetStateAction<string[]>>;

  data: string[][];
  setData: Dispatch<SetStateAction<string[][]>>;
}

export function REPLInput(props: REPLInputProps) {
  // Manages the contents of the input box
  const [commandString, setCommandString] = useState<string>("");
  // Manages the current amount of times the button is clicked
  const [count, setCount] = useState<number>(0);

  const [isBriefMode, setIsBriefMode] = useState<boolean>(true);

  const [currentFilePath, setCurrentFilePath] = useState<string>("");

  function seperateCommand(command: string) {
    let briefMode = false;
    let output = "";
    let splicedCommand = command.split(" ");

    hashmapData();
    if (splicedCommand.length > 3) {
      printCommand(command, isBriefMode, "Command too long!");
      // check if base command is mode
    } else if (splicedCommand[0] === "mode") {
      modeSwitch(splicedCommand, output, briefMode, command);
    } else {
      // check if base command is load_file
      if (splicedCommand[0] === "load_file") {
        loadFile(splicedCommand, output, command);
        // check if base command is view
      } else if (splicedCommand[0] === "view") {
        view(splicedCommand, output, command);
        // check if base command is search
      } else if (command.split(" ")[0] === "search") {
        search(splicedCommand, output, command);
        // if user enters a command that does not correspond to anything
      } else {
        printCommand(command, isBriefMode, "Unknown input, please try again.");
      }
    }
  }

  // helper func to switch the mode
  function modeSwitch(
    splicedCommand: string[],
    output: string,
    briefMode: boolean,
    command: string
  ) {
    // checks if user input more that just "mode"
    if (splicedCommand.length > 1) {
      output = "Command too long!";
      // check if the mode is currently brief or mode, toggles it accordingly
    } else if (isBriefMode) {
      briefMode = false;
      setIsBriefMode(false);
      output = "Mode switched to verbose";
    } else {
      briefMode = true;
      setIsBriefMode(true);
      output = "Mode switched to brief";
    }
    printCommand(command, briefMode, output);
  }

  // helper func to load a file
  function loadFile(splicedCommand: string[], output: string, command: string) {
    let arrays = returnArrayMap();
    //if the length is too long for the command
    if (splicedCommand.length != 2) {
      output = "Incorrect command length, check filepath!";
    } else {
      if (arrays !== undefined) {
        console.log(splicedCommand[1]);
        console.log(arrays.get(splicedCommand[1]));
        //if there is no such filepath in our mock data
        if (!arrays.has(splicedCommand[1])) {
          output = "File not found!";
        } else {
          //if there is a filepath found
          setCurrentFilePath(splicedCommand[1]);
          output = "File: '" + splicedCommand[1] + "' loaded";
        }
      }
    }
    printCommand(command, isBriefMode, output);
  }

  // helper func to view the loaded file in an html table
  function view(splicedCommand: string[], output: string, command: string) {
    //if the user has not loaded a file before viewing
    if (currentFilePath === "") {
      output = "Please load a file first!";
    } else if (splicedCommand.length != 1) {
      output =
        "Command too long! Use only 'view' to see the current loaded file";
    } else {
      //if there is a filepath such that we can view
      let array = returnArrayMap().get(currentFilePath);
      if (array !== undefined) {
        props.setData(array);
        output = "File: '" + currentFilePath + "' available for viewing below";
      }
    }
    printCommand(command, isBriefMode, output);
  }

  // helper func to search
  function search(splicedCommand: string[], output: string, command: string) {
    if (currentFilePath === "") {
      output = "Please load a file first!";
    } else if (splicedCommand.length == 1 || splicedCommand.length > 3) {
      output =
        "Incorrect command length, please search with a header and value or just a value";
    } else {
      let value = "";
      // if the command is three words long, the user wants to search with headers
      if (splicedCommand.length == 3) {
        let column = splicedCommand[1];
        value = splicedCommand[2];
        let outerMap = returnSearchMap().get(currentFilePath);

        if (outerMap !== undefined) {
          let innerMap = outerMap.get(column + " " + value);
          console.log(column + " " + value);
          if (innerMap !== undefined) {
            props.setData(innerMap);
          }
        }
        output = "Searched for " + value + " in column " + column + " below";
        // if the command is only two words long, the user does not want to search with headers
      } else {
        //if the path is found and there is informaton
        value = splicedCommand[1];
        let outerMap = returnSearchMap().get(currentFilePath);
        if (outerMap !== undefined) {
          let innerMap = outerMap.get(value);
          if (innerMap !== undefined) {
            props.setData(innerMap);
          }
        }
        output = "Searched for " + value + " below";
      }
    }
    printCommand(command, isBriefMode, output);
  }

  // helper func to print the output in the correct mode, either verbose or brief, to the history
  function printCommand(
    commandString: string,
    briefMode: boolean,
    output: string
  ) {
    if (briefMode) {
      props.setHistory([...props.history, "Output: " + output]);
    } else {
      props.setHistory([
        ...props.history,
        "Command: " + commandString + ", Output: " + output,
      ]);
    }
  }

  // This function is triggered when the button is clicked.
  function handleSubmit(commandString: string) {
    setCount(count + 1);
    seperateCommand(commandString);
    setCommandString("");
  }

  /**
   * returns a JSX div to be displayed by the REPL and then the App
   */
  return (
    <div className="repl-input">
      <fieldset>
        <legend>Enter a command:</legend>
        <ControlledInput
          value={commandString}
          setValue={setCommandString}
          ariaLabel={"Command input"}
        />
      </fieldset>

      <button onClick={() => handleSubmit(commandString)}>
        Submitted {count} times
      </button>
    </div>
  );
}
