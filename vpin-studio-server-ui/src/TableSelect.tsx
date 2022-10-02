import * as React from "react";
import Button from '@material-ui/core/Button';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';


const styles = {
  table: {
    minWidth: 650,
  },
};


export default class TableSelector extends React.Component {
  state = {
    tables: [],
    tableName: "",
    rom: null,
    table: null,
    tableOfTheMonth: ""
  };

  handleChange = (event: React.ChangeEvent<{ value: any }>) => {
    let tables = this.state.tables;
    let v = event.target.value;
    for (let t of tables) {
      if (t['romName'] === v) {
        this.setState({
          tableName: t['name'],
          rom: t['romName'],
          table: t
        });
      }
    }
  };

  onTableSet = () => {
    fetch('/setTable', {
      method: 'post',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: "rom=" + this.state.rom
     })
     .then(response => {
      let tables = this.state.tables;
      for (let t of tables) {
        if (t['romName'] === this.state.rom) {
          this.setState({
            tableOfTheMonth: t['name']
          });
        }
      }
     });
  }

  componentDidMount() {
    fetch('/data')
      .then(response => response.json())
      .then(response => {
        if(response.pinballOfTheMonth) {
          this.setState({
            tables: response.tables,
            rom: response.pinballOfTheMonth.romName,
            table: response.pinballOfTheMonth,
            tableName: response.pinballOfTheMonth.name,
            tableOfTheMonth: response.pinballOfTheMonth.name
          });
        }
        else {
          this.setState({
            tables: response.tables          
          });
        }
      })
      .catch(error => this.setState({

      }));
  }

  render() {
    const classes = this.props;
    let tables = this.state.tables;
    let table: any = this.state.table;

    return <div>
      <Typography variant="h6" component="h1" gutterBottom>
        Table of the Month: {this.state.tableOfTheMonth}
      </Typography>

      <Typography style={{marginTop: 24}}>Select Table:</Typography>
      <Select
        style={{ marginTop: 0, marginBottom: 12, marginRight: 12, marginLeft: 0, minWidth: 200 }}
        labelId="month-table-label"
        id="month-table"
        value={this.state.rom}
        onChange={this.handleChange}>
        {
          tables.map((table, i) =>
            <MenuItem key={i} value={table['romName']}>{table['name']}</MenuItem>
          )
        }
      </Select>
      <Button variant="contained" color="primary" onClick={this.onTableSet}>Set as 'Table of the Month'</Button>


      {table &&
        <div>
          <Typography style={{ marginBottom: 12, marginTop: 24 }}>Scores of Table: {this.state.tableName}</Typography>
          <TableContainer component={Paper} style={{ paddingTop: 12 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Player</TableCell>
                  <TableCell align="right">Score</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {table['highscore'] && table['highscore']['scores'].map((score: any, i: number) => (
                  <TableRow key={i}>
                    <TableCell component="th" scope="row">
                      {score.userInitials}
                    </TableCell>
                    <TableCell align="right">{score.score}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      }
    </div>;
  }


}


