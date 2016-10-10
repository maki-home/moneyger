import React, {Component} from "react";
import {Link} from "react-router";
import {BasicPanelAlt} from "pui-react-panels";
import {Row, Col} from "pui-react-grids";
import {DefaultButton} from "pui-react-buttons";
import {Icon} from "pui-react-iconography";
import OutcomeForm from "./OutcomeForm";
import OutcomeSearchForm from "./OutcomeSearchForm";
import OutcomeTable from "./OutcomeTable";
import moment from "moment";
import outcomeClient from "./OutcomeClient";

class Outcomes extends Component {
    constructor(props) {
        super(props);
        const outcomeDate = props.outcomeDate || moment();
        this.state = {
            data: [],
            outcomeDate: outcomeDate,
            prevDate: outcomeDate.clone().subtract(1, 'd'),
            nextDate: outcomeDate.clone().add(1, 'd')
        };
    }

    addOutcome(outcome) {
        return outcomeClient.addOutcome(outcome)
            .then(o => {
                this.state.data.push(o);
                this.setState({
                    data: this.state.data
                });
                return o;
            })
    }

    render() {
        return (<div>
            <BasicPanelAlt header="支出登録">
                <OutcomeForm addOutcome={this.addOutcome.bind(this)}
                             outcomeDate={this.state.outcomeDate}
                             fetchUserinfo={this.props.fetchUserinfo}
                             fetchMembers={this.props.fetchMembers}
                             fetchOutcomeCategories={this.props.fetchOutcomeCategories}/>
            </BasicPanelAlt>
            <BasicPanelAlt header="簡易支出検索">
                <OutcomeSearchForm outcomeDate={this.state.outcomeDate}/>
            </BasicPanelAlt>
            <BasicPanelAlt header="支出一覧">
                <Row>
                    <Col xs={4}><DefaultButton>
                        <Link to={`/outcomes/${this.state.prevDate.format('YYYY-MM-DD')}`}>
                            <Icon name="angle-double-left"/>&nbsp;{this.state.prevDate.format('YYYY-MM-DD')}
                        </Link>
                    </DefaultButton></Col>
                    <Col xs={6}/>
                    <Col xs={4}><DefaultButton>
                        <Link to={{
                            pathname: '/report',
                            query: {
                                fromDate: this.state.outcomeDate.format('YYYY-MM')
                            }
                        }}>{this.state.outcomeDate.format('YYYY-MM')}のレポート</Link>
                    </DefaultButton></Col>
                    <Col xs={6}/>
                    <Col xs={4}><DefaultButton>
                        <Link to={`/outcomes/${this.state.nextDate.format('YYYY-MM-DD')}`}>
                            {this.state.nextDate.format('YYYY-MM-DD')}&nbsp;<Icon name="angle-double-right"/>
                        </Link>
                    </DefaultButton></Col>
                </Row>
                <OutcomeTable data={this.state.data}
                              outcomeDate={this.state.outcomeDate}
                              fetchMembers={this.props.fetchMembers}/>
            </BasicPanelAlt>
        </div>);
    }

    componentWillReceiveProps(props) {
        const outcomeDate = props.outcomeDate || moment();
        this.setState({
            outcomeDate: outcomeDate,
            prevDate: outcomeDate.clone().subtract(1, 'd'),
            nextDate: outcomeDate.clone().add(1, 'd')
        });
        outcomeClient.findByOutcomeDate(outcomeDate)
            .then(data => {
                this.setState({
                    data: data
                });
            })
    }

}

export default Outcomes;