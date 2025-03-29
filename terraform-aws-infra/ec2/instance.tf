resource "aws_instance" "react_server" {
  ami                    = "ami-12345678"
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.private.id
  security_groups        = [aws_security_group.allow_outbound.id]
  user_data              = <<-EOF
                #!/bin/bash
                aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
                docker pull $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:latest
                docker run -d -p 80:80 $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:latest
                EOF
}
