import React from "react";
import {Router, Route, browserHistory} from "react-router";
import MainTabs from "./MainTabs";

const Bar = () => (
    <div className="container">
        <p>Bar</p>
    </div>
);

const NotFound = () => (
    <div className="container">
        <p>Not Found</p>
    </div>
);

const App = () => (
    <Router history={browserHistory}>
        <Route path="/" component={MainTabs}/>
        <Route path="/index.html" component={MainTabs}/>
        <Route path="/outcomes(/:outcomeDate)" component={MainTabs}/>
        <Route path="/incomes" component={MainTabs}/>
        <Route path="/report" component={MainTabs}/>
        <Route path="/bar" component={Bar}/>
        <Route path="*" component={NotFound}/>
    </Router>
);

export default App;
