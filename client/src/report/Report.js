import React, {Component} from "react";
import {Link} from "react-router";
import {Table, TableCell} from "pui-react-table";
import {BasicPanelAlt} from "pui-react-panels";
import format from "number-format.js";

class AmountCell extends Component {
    render() {
        return (
            <TableCell>
                {format('#,##0.####', this.props.value)}円
            </TableCell>
        );
    }
}

let columnsByDate = [
    {
        attribute: 'outcomeDate',
        displayName: '支出日',
        sortable: true
    },
    {
        attribute: 'amount',
        displayName: '金額',
        sortable: true,
        CustomCell: AmountCell
    }
];
let dataByDate = [
    {
        outcomeDate: '2016-10-01',
        amount: 65959
    },
    {
        outcomeDate: '2016-10-02',
        amount: 342
    },
    {
        outcomeDate: '2016-10-04',
        amount: 2124
    }
];
let summaryByDate = [
    {
        attribute: 'total',
        displayName: '合計',
        sortable: false
    },
    {
        attribute: 'amount',
        displayName: format('#,##0.####', dataByDate.map(x => x.amount).reduce((x, y) => x + y, 0)) + '円',
        sortable: false,
    }
];

let columnsByCategory = [
    {
        attribute: 'category',
        displayName: 'カテゴリ',
        sortable: true
    },
    {
        attribute: 'amount',
        displayName: '金額',
        sortable: true,
        CustomCell: AmountCell
    }];

let dataByCategory = [
    {
        category: '食費',
        amount: 462
    },
    {
        category: '日用品',
        amount: 53784
    },
    {
        category: '教育・教養',
        amount: 2124
    },
    {
        category: '交通・通信',
        amount: 3000
    },
    {
        category: '医療',
        amount: 2220
    },
    {
        category: '交際',
        amount: 6715
    }
];
let summaryByCategory = [
    {
        attribute: 'total',
        displayName: '合計',
        sortable: false
    },
    {
        attribute: 'amount',
        displayName: format('#,##0.####', dataByCategory.map(x => x.amount).reduce((x, y) => x + y, 0)) + '円',
        sortable: false,
    }
];

class Report extends Component {
    render() {
        return (
            <div>
                <BasicPanelAlt header="収支報告">
                </BasicPanelAlt>
                <BasicPanelAlt header="支出">
                    <Link to={{
                        pathname: '/outcomes',
                        query: {

                        }
                    }}>この期間の全支出</Link>
                    <Table columns={columnsByDate} data={dataByDate}/>
                    <Table columns={summaryByDate} data={[]}/>
                    <Table columns={columnsByCategory} data={dataByCategory}/>
                    <Table columns={summaryByCategory} data={[]}/>
                </BasicPanelAlt>
                <BasicPanelAlt header="収入">
                    <Table columns={columnsByDate} data={dataByDate}/>
                    <Table columns={summaryByDate} data={[]}/>
                </BasicPanelAlt>
            </div>
        );
    }
}


export default Report;