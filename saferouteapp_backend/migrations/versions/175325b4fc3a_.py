"""empty message

Revision ID: 175325b4fc3a
Revises: 597cf160ab1
Create Date: 2017-04-18 23:53:00.801378

"""

# revision identifiers, used by Alembic.
revision = '175325b4fc3a'
down_revision = '597cf160ab1'

from alembic import op
import sqlalchemy as sa


def upgrade():
    ### commands auto generated by Alembic - please adjust! ###
    op.create_table('history',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('created', sa.DateTime(), nullable=True),
    sa.Column('modified', sa.DateTime(), nullable=True),
    sa.Column('src_latitude', sa.String(length=255), nullable=False),
    sa.Column('src_longitude', sa.String(length=255), nullable=False),
    sa.Column('dest_latitude', sa.String(length=255), nullable=False),
    sa.Column('dest_longitude', sa.String(length=255), nullable=False),
    sa.Column('user_id', sa.Integer(), nullable=False),
    sa.PrimaryKeyConstraint('id')
    )
    ### end Alembic commands ###


def downgrade():
    ### commands auto generated by Alembic - please adjust! ###
    op.drop_table('history')
    ### end Alembic commands ###
