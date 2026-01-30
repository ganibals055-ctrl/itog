using Microsoft.EntityFrameworkCore;
using RatingCommentsService.Models;

namespace RatingCommentsService.Data;

public class AppDb : DbContext
{
    public AppDb(DbContextOptions<AppDb> options) : base(options) { }

    public DbSet<DogRating> Ratings => Set<DogRating>();
    public DbSet<DogComment> Comments => Set<DogComment>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<DogRating>(b =>
        {
            b.ToTable("dog_ratings");

            b.HasKey(x => x.Id);

            b.Property(x => x.Id)
                .ValueGeneratedOnAdd();

            b.Property(x => x.DogId)
                .IsRequired();

            b.Property(x => x.Value)
                .IsRequired();

            b.Property(x => x.CreatedAt)
                .HasDefaultValueSql("NOW()");
        });

        modelBuilder.Entity<DogComment>(b =>
        {
            b.ToTable("dog_comments");

            b.HasKey(x => x.Id);

            b.Property(x => x.Id)
                .ValueGeneratedOnAdd();

            b.Property(x => x.DogId)
                .IsRequired();

            b.Property(x => x.Text)
                .HasColumnType("text")
                .IsRequired();

            b.Property(x => x.CreatedAt)
                .HasDefaultValueSql("NOW()");
        });
    }
}
