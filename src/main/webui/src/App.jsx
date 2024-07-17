import {BryntumSchedulerPro} from "@bryntum/schedulerpro-react";
import {schedulerProps} from "./SchedulerConfig";
import "./App.scss";

function App() {
    return (
        <>
            <BryntumSchedulerPro {...schedulerProps} />
        </>
    )
}

export default App;