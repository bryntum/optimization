import {BryntumScheduler} from "@bryntum/scheduler-react";
import {schedulerProps} from "./SchedulerConfig";
import "./App.scss";
import TimefoldSolver from "./components/TimefoldSolver.jsx";
import TimefoldReset from "./components/TimefoldReset.jsx";

function App() {
    return (
        <>
            <TimefoldSolver/>
            <TimefoldReset/>
            <BryntumScheduler {...schedulerProps} />;
        </>
    )
}

export default App;