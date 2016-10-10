import React, {Component} from "react";
import {Link} from "react-router";
import {Tabs, Tab} from "pui-react-tabs";
import {Panel} from "pui-react-panels";
import {Icon} from "pui-react-iconography";
import Outcomes from "./outcomes/Outcomes";
import Report from "./report/Report";
import memberClient from "./members/MemberClient";
import outcomeClient from "./outcomes/OutcomeClient";
import moment from "moment";

class MainTabs extends Component {
    static keyToPathMap = {
        1: '/outcomes',
        2: '/incomes',
        3: '/report'
    };

    constructor(props) {
        super(props);
        this.state = {
            activeKey: this.pathToKey(props.route.path),
            userinfo: {name: {}},
            fetchUserinfo: memberClient.fetchUserinfo(),
            fetchMembers: memberClient.fetchMembers(),
            fetchOutcomeCategories: outcomeClient.fetchOutcomeCategories()
        };
        this.onEntered = this.onEntered.bind(this);
    }

    pathToKey(path) {
        for (let k in MainTabs.keyToPathMap) {
            if (path.startsWith(MainTabs.keyToPathMap[k])) {
                return Number(k);
            }
        }
        return 1;
    }

    keyToPath(key) {
        return MainTabs.keyToPathMap[key];
    }

    componentDidMount() {
        this.state.fetchUserinfo
            .then(userinfo => {
                this.setState({
                    userinfo: userinfo
                });
            });
    }

    componentWillReceiveProps(props) {
        this.setState({
            activeKey: this.pathToKey(props.route.path)
        });
    }

    onEntered(key) {
        setTimeout(()=> {
            // avoid changing state during transition
            this.setState({
                activeKey: key
            });
        }, 10);
    }

    render() {
        return (
            <div className="container">
                <Panel header={<h2><Link to="/">家計簿</Link></h2>}>
                    <Tabs defaultActiveKey={this.state.activeKey}
                          activeKey={this.state.activeKey}
                          actions={<Link to="/bar"><Icon name="user"/>&nbsp;
                              {`${this.state.userinfo.name.familyName} ${this.state.userinfo.name.givenName}`}
                          </Link>}>
                        <Tab eventKey={1} onEntered={this.onEntered} title="支出">
                            <Outcomes outcomeDate={moment(this.props.params.outcomeDate)}
                                      fetchUserinfo={this.state.fetchUserinfo}
                                      fetchMembers={this.state.fetchMembers}
                                      fetchOutcomeCategories={this.state.fetchOutcomeCategories}/>
                        </Tab>
                        <Tab eventKey={2} onEntered={this.onEntered} title="収入">
                            <h2>Neat!</h2>
                            <span>So much content.</span>
                        </Tab>
                        <Tab eventKey={3} onEntered={this.onEntered} title="今月のレポート">
                            <Report fromDate={moment(this.props.location.query.fromDate)}
                                    toDate={moment(this.props.location.query.toDate)}/>
                        </Tab>
                    </Tabs>
                </Panel>
            </div>
        );
    }
}

export default MainTabs;