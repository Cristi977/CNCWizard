# CNC Program Parser & Wizard Suite

A __Java__-based application designed to parse, manage, and safely __update__ CNC program files, subprograms, and macro variables with built-in, synchronize .

## Overview
This software translates CNC programs into structured data, tracks machine states across sequence blocks, handles subprogram files (`.anc`), and provides interactive Swing table interfaces to safely modify machining parameters and tool offsets.

## Core Components

* **`GCodeParser.java`**: Translates the CNC program into specific structures, handles macro expressions (such as `E80050` and `E30050`), manages subprogram lookups via `CLS` commands, and performs automated tool synchronization. [GCodeParser.java](https://github.com/Cristi977/CNCWizard/blob/main/src/GCodeParser.java).
* **`MachineState.java`**: Saves parsed macro states and inherits machine parameters across sequence blocks, managing variables like `E80050`, `G178` and tool index `T` mappings. [MachineState.java](https://github.com/Cristi977/CNCWizard/blob/main/src/MachineState.java).
* **`TableModel.java`**: Defines the editable Swing table structure for sequence codes, tool usage, editable tool indices, and selected tables, writing updates directly back to physical files safely. [TableModel.java](https://github.com/Cristi977/CNCWizard/blob/main/src/TableModel.java).
* **`WizardFile.java`**: Handles graphical file selection and search routines for locating specific CNC programs based on product codes and types.

## Key Features

* **Automated Tool-Index Syncing**: Cross-references active tool calls (`T`) with macro configurations (`E80050`) to prevent machine indexing errors.
* **Subprogram File Management**: Automatically scans and parses related subprogram files (`.anc`), tracking file paths and validating states. [GCodeParser.java](https://github.com/Cristi977/CNCWizard/blob/main/src/GCodeParser.java) and [MachineState.java](https://github.com/Cristi977/CNCWizard/blob/main/src/MachineState.java).
* **Interactive Editing**: Modify tool indices and table values via an editable interface that updates both runtime memory and disk files simultaneously. [TableModel.java](https://github.com/Cristi977/CNCWizard/blob/main/src/TableModel.java) and [WizardOffset.java](https://github.com/Cristi977/CNCWizard/blob/main/src/WizardOffset.java).

## License

Copyright © 2026 Cristian Boitor.  
Licensed under the **Apache License, Version 2.0**; you may not use these files except in compliance with the License. You may obtain a copy of the License at: </br></br> http://www.apache.org/licenses/LICENSE-2.0 </br></br> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an `"AS IS" BASIS`, `WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND`, either express or implied. See the License for the specific language governing permissions and limitations under the License.
