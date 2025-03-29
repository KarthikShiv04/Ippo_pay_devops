module "vpc" {
  source = "./vpc"
}

module "ec2" {
  source = "./ec2"
  private_subnet_id = module.vpc.private_subnet_id
  security_group_id = module.vpc.security_group_id
}

module "ecr" {
  source = "./ecr"
}