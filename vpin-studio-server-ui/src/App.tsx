import React from 'react';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import TableSelect from './TableSelect';


function Header() {
  return (
   <Typography variant="h3" component="h1" gutterBottom>
      CorePin Admin
    </Typography>
  );
}

export default function App() {
  return (
    <Container maxWidth="sm">
      <Box my={4}>
      <Header />
      <TableSelect />
      </Box>
    </Container>
  );
}
