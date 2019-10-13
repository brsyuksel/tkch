package tkch.products.repo

import tkch.products.Product

trait ProductRepo[F[_]] {
  def create(p: Product): F[Product]
  def getById(id: Long): F[Option[Product]]
  def list(offset: Int, limit: Int): F[List[Product]]
  def listByCategoryId(cId: Long, offset: Int, limit: Int): F[List[Product]]
  def update(p: Product): F[Product]
  def delete(id: Long): F[Unit]
}
