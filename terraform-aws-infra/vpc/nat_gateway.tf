resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id
}

resource "aws_nat_gateway" "nat" {
  subnet_id = aws_subnet.public.id
  allocation_id = aws_eip.nat.id
}

resource "aws_eip" "nat" {
  vpc = true
}
