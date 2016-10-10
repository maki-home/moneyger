import React, {Component} from "react";
import {Link} from "react-router";
import {Table, TableCell} from "pui-react-table";
import {BasicPanelAlt} from "pui-react-panels";
import {Row, Col} from "pui-react-grids";
import {DefaultButton} from "pui-react-buttons";
import {Icon} from "pui-react-iconography";
import outcomeClient from "../outcomes/OutcomeClient";
import format from "number-format.js";
import moment from "moment";

class AmountCell extends Component {
    render() {
        return (
            <TableCell>
                {format('#,##0.####', this.props.value)}円
            </TableCell>
        );
    }
}

const columnsByDate = [
    {
        attribute: 'outcomeDate',
        displayName: '支出日',
        sortable: true
    },
    {
        attribute: 'subTotal',
        displayName: '金額',
        sortable: true,
        CustomCell: AmountCell
    }
];


const columnsByCategory = [
    {
        attribute: 'parentCategoryName',
        displayName: 'カテゴリ',
        sortable: true,
        sortBy: value => value.parentCategoryId
    },
    {
        attribute: 'subTotal',
        displayName: '金額',
        sortable: true,
        CustomCell: AmountCell
    }];


class Report extends Component {

    constructor(props) {
        super(props);
        let state = this.range(this.props.fromDate, this.props.toDate);
        state.reportByDate = [];
        state.reportByParentCategory = [];
        this.state = state;
    }

    range(from, to) {
        let fromDate = from || moment().startOf('month');
        return {
            fromDate: fromDate,
            toDate: (to || fromDate.clone().endOf('month'))
        }
    }

    render() {
        const prevMonth = this.state.fromDate.clone().subtract(1, 'month'),
            nextMonth = this.state.fromDate.clone().add(1, 'month');
        return (
            <div>
                <h2>
                    {this.state.fromDate.format('YYYY-MM-DD')} ~ {this.state.toDate.format('YYYY-MM-DD')}のレポート
                </h2>
                <Row>
                    <Col xs={4}><DefaultButton>
                        <Link to={{
                            pathname: '/report', query: {
                                fromDate: prevMonth.format('YYYY-MM')
                            }
                        }}>
                            <Icon name="angle-double-left"/>&nbsp;{prevMonth.format('YYYY-MM')}
                        </Link>
                    </DefaultButton></Col>
                    <Col xs={16}/>
                    <Col xs={4}><DefaultButton>
                        <Link to={{
                            pathname: '/report', query: {
                                fromDate: nextMonth.format('YYYY-MM')
                            }
                        }}>
                            {nextMonth.format('YYYY-MM')}&nbsp;<Icon name="angle-double-right"/>
                        </Link>
                    </DefaultButton></Col>
                </Row>
                <BasicPanelAlt header="収支報告">
                </BasicPanelAlt>
                <BasicPanelAlt header="支出">
                    <Link to={{
                        pathname: '/outcomes',
                        query: {}
                    }}>この期間の全支出</Link>
                    <Table columns={columnsByDate} data={this.state.reportByDate}/>
                    <Table columns={[
                        {
                            attribute: 'total',
                            displayName: '合計',
                            sortable: false
                        },
                        {
                            attribute: 'amount',
                            displayName: format('#,##0.####', this.state.reportByDate.map(x => x.subTotal).reduce((x, y) => x + y, 0)) + '円',
                            sortable: false,
                        }
                    ]} data={[]}/>
                    <Table columns={columnsByCategory} data={this.state.reportByParentCategory}/>
                    <Table columns={[
                        {
                            attribute: 'total',
                            displayName: '合計',
                            sortable: false
                        },
                        {
                            attribute: 'amount',
                            displayName: format('#,##0.####', this.state.reportByParentCategory.map(x => x.subTotal).reduce((x, y) => x + y, 0)) + '円',
                            sortable: false,
                        }
                    ]} data={[]}/>
                </BasicPanelAlt>
                <BasicPanelAlt header="収入">
                </BasicPanelAlt>
            </div>
        );
    }

    componentWillReceiveProps(props) {
        let range = this.range(props.fromDate, props.toDate);
        Promise.all([
            outcomeClient.reportByDate(range.fromDate, range.toDate),
            outcomeClient.reportByParentCategory(range.fromDate, range.toDate)
        ]).then(ret => {

            range.reportByDate = ret[0];
            range.reportByParentCategory = ret[1];
            this.setState(range);
        });


    }
}


export default Report;